package roguelike.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.function.Consumer;

public final class PostProcessor implements Disposable {
    public static final float BLOOM_THRESHOLD = 0.6f;
    public static final float BLOOM_INTENSITY = 1.1f;
    public static final float VIGNETTE_STRENGTH = 0.55f;
    public static final float BASE_ABERRATION = 0.0015f;
    public static final int BLOOM_DOWNSCALE = 4;
    public static final int BLUR_ITERATIONS = 3;
    public static final int MAXIMUM_RIPPLES = 4;
    public static final float RIPPLE_STRENGTH = 0.018f;

    private static final String VERTEX_SHADER = """
            attribute vec4 a_position;
            attribute vec4 a_color;
            attribute vec2 a_texCoord0;
            uniform mat4 u_projTrans;
            varying vec2 v_texCoords;
            void main() {
                v_texCoords = a_texCoord0;
                gl_Position = u_projTrans * a_position;
            }
            """;

    private static final String THRESHOLD_SHADER = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform float u_threshold;
            void main() {
                vec3 color = texture2D(u_texture, v_texCoords).rgb;
                float brightness = max(color.r, max(color.g, color.b));
                float contribution = smoothstep(u_threshold, u_threshold + 0.35, brightness);
                gl_FragColor = vec4(color * contribution, 1.0);
            }
            """;

    private static final String BLUR_SHADER = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform vec2 u_direction;
            void main() {
                vec3 sum = texture2D(u_texture, v_texCoords).rgb * 0.2270270270;
                sum += texture2D(u_texture, v_texCoords + u_direction * 1.3846153846).rgb * 0.3162162162;
                sum += texture2D(u_texture, v_texCoords - u_direction * 1.3846153846).rgb * 0.3162162162;
                sum += texture2D(u_texture, v_texCoords + u_direction * 3.2307692308).rgb * 0.0702702703;
                sum += texture2D(u_texture, v_texCoords - u_direction * 3.2307692308).rgb * 0.0702702703;
                gl_FragColor = vec4(sum, 1.0);
            }
            """;

    private static final String COMPOSITE_SHADER = """
            #ifdef GL_ES
            precision mediump float;
            #endif
            varying vec2 v_texCoords;
            uniform sampler2D u_texture;
            uniform sampler2D u_bloom;
            uniform float u_bloomIntensity;
            uniform float u_aberration;
            uniform float u_vignette;
            uniform float u_hurt;
            uniform float u_flash;
            uniform float u_time;
            uniform vec4 u_ripples[4];
            uniform float u_aspect;
            void main() {
                vec2 uv = v_texCoords;
                for (int index = 0; index < 4; index++) {
                    vec4 ripple = u_ripples[index];
                    vec2 toPixel = (v_texCoords - ripple.xy) * vec2(u_aspect, 1.0);
                    float distanceToCenter = length(toPixel);
                    float band = 1.0 - smoothstep(0.0, 0.05, abs(distanceToCenter - ripple.z));
                    uv -= toPixel / max(distanceToCenter, 0.0001) / vec2(u_aspect, 1.0) * band * ripple.w;
                }
                vec2 offset = (uv - 0.5) * u_aberration;
                vec3 color;
                color.r = texture2D(u_texture, uv + offset).r;
                color.g = texture2D(u_texture, uv).g;
                color.b = texture2D(u_texture, uv - offset).b;
                color += texture2D(u_bloom, uv).rgb * u_bloomIntensity;
                float luminance = dot(color, vec3(0.299, 0.587, 0.114));
                color = mix(vec3(luminance), color, 1.15);
                color = (color - 0.5) * 1.06 + 0.5;
                color *= vec3(1.0, 0.97, 1.04);
                vec2 fromCenter = v_texCoords - 0.5;
                float edge = length(fromCenter) * 1.4142;
                float edgeMask = smoothstep(0.35, 1.0, edge);
                color *= 1.0 - u_vignette * edgeMask;
                color = mix(color, vec3(0.75, 0.04, 0.08), u_hurt * edgeMask * 0.75);
                color += vec3(u_flash * 0.55);
                float grain = fract(sin(dot(v_texCoords * 913.7 + u_time, vec2(12.9898, 78.233))) * 43758.5453);
                color += (grain - 0.5) * 0.035;
                gl_FragColor = vec4(clamp(color, 0.0, 1.0), 1.0);
            }
            """;

    private final SpriteBatch batch = new SpriteBatch(4);
    private final Matrix4 screenProjection = new Matrix4().setToOrtho2D(0f, 0f, 1f, 1f);
    private final float[] rippleUniforms = new float[MAXIMUM_RIPPLES * 4];
    private final ShaderProgram thresholdShader;
    private final ShaderProgram blurShader;
    private final ShaderProgram compositeShader;
    private FrameBuffer sceneBuffer;
    private FrameBuffer bloomBuffer;
    private FrameBuffer blurBuffer;
    private int bufferWidth;
    private int bufferHeight;

    public PostProcessor() {
        ShaderProgram.pedantic = false;
        thresholdShader = compile(THRESHOLD_SHADER);
        blurShader = compile(BLUR_SHADER);
        compositeShader = compile(COMPOSITE_SHADER);
        batch.setProjectionMatrix(screenProjection);
    }

    public void beginScene(Viewport viewport) {
        int width = Math.max(1, viewport.getScreenWidth());
        int height = Math.max(1, viewport.getScreenHeight());
        if (width != bufferWidth || height != bufferHeight) {
            disposeBuffers();
            bufferWidth = width;
            bufferHeight = height;
            sceneBuffer = createBuffer(width, height);
            bloomBuffer = createBuffer(Math.max(1, width / BLOOM_DOWNSCALE), Math.max(1, height / BLOOM_DOWNSCALE));
            blurBuffer = createBuffer(Math.max(1, width / BLOOM_DOWNSCALE), Math.max(1, height / BLOOM_DOWNSCALE));
        }
        sceneBuffer.begin();
        ScreenUtils.clear(0f, 0f, 0f, 1f);
    }

    public void endScene(Viewport viewport, float shake, float hurt, float flash, float time, float[] worldRipples) {
        sceneBuffer.end();
        Camera camera = viewport.getCamera();
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float visibleLeft = camera.position.x - worldWidth / 2f;
        float visibleBottom = camera.position.y - worldHeight / 2f;
        for (int offset = 0; offset < rippleUniforms.length; offset += 4) {
            rippleUniforms[offset] = (worldRipples[offset] - visibleLeft) / worldWidth;
            rippleUniforms[offset + 1] = (worldRipples[offset + 1] - visibleBottom) / worldHeight;
            rippleUniforms[offset + 2] = worldRipples[offset + 2] / worldHeight;
            rippleUniforms[offset + 3] = worldRipples[offset + 3] * RIPPLE_STRENGTH;
        }
        Gdx.gl.glDisable(GL20.GL_BLEND);
        drawPass(bloomBuffer, thresholdShader, sceneBuffer.getColorBufferTexture(), shader -> shader.setUniformf("u_threshold", BLOOM_THRESHOLD));
        float texelWidth = 1f / bloomBuffer.getWidth();
        float texelHeight = 1f / bloomBuffer.getHeight();
        for (int iteration = 1; iteration <= BLUR_ITERATIONS; iteration++) {
            float spread = iteration;
            drawPass(blurBuffer, blurShader, bloomBuffer.getColorBufferTexture(), shader -> shader.setUniformf("u_direction", texelWidth * spread, 0f));
            drawPass(bloomBuffer, blurShader, blurBuffer.getColorBufferTexture(), shader -> shader.setUniformf("u_direction", 0f, texelHeight * spread));
        }

        ScreenUtils.clear(0f, 0f, 0f, 1f);
        viewport.apply();
        bloomBuffer.getColorBufferTexture().bind(1);
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        batch.setShader(compositeShader);
        batch.disableBlending();
        batch.begin();
        compositeShader.setUniformi("u_bloom", 1);
        compositeShader.setUniformf("u_bloomIntensity", BLOOM_INTENSITY);
        compositeShader.setUniformf("u_aberration", BASE_ABERRATION + shake * 0.01f + hurt * 0.012f);
        compositeShader.setUniformf("u_vignette", VIGNETTE_STRENGTH);
        compositeShader.setUniformf("u_hurt", hurt);
        compositeShader.setUniformf("u_flash", flash);
        compositeShader.setUniformf("u_time", time % 100f);
        compositeShader.setUniform4fv("u_ripples[0]", rippleUniforms, 0, rippleUniforms.length);
        compositeShader.setUniformf("u_aspect", worldWidth / worldHeight);
        batch.draw(sceneBuffer.getColorBufferTexture(), 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f);
        batch.end();
        Gdx.gl.glEnable(GL20.GL_BLEND);
    }

    @Override
    public void dispose() {
        disposeBuffers();
        batch.dispose();
        thresholdShader.dispose();
        blurShader.dispose();
        compositeShader.dispose();
    }

    private void drawPass(FrameBuffer target, ShaderProgram shader, Texture source, Consumer<ShaderProgram> setUniforms) {
        target.begin();
        batch.setShader(shader);
        batch.disableBlending();
        batch.begin();
        setUniforms.accept(shader);
        batch.draw(source, 0f, 0f, 1f, 1f, 0f, 0f, 1f, 1f);
        batch.end();
        target.end();
    }

    private void disposeBuffers() {
        if (sceneBuffer != null) {
            sceneBuffer.dispose();
            bloomBuffer.dispose();
            blurBuffer.dispose();
        }
    }

    private static FrameBuffer createBuffer(int width, int height) {
        FrameBuffer buffer = new FrameBuffer(Pixmap.Format.RGBA8888, width, height, false);
        buffer.getColorBufferTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        buffer.getColorBufferTexture().setWrap(Texture.TextureWrap.ClampToEdge, Texture.TextureWrap.ClampToEdge);
        return buffer;
    }

    private static ShaderProgram compile(String fragmentShader) {
        ShaderProgram shader = new ShaderProgram(VERTEX_SHADER, fragmentShader);
        if (!shader.isCompiled()) {
            throw new GdxRuntimeException("Shader failed to compile: " + shader.getLog());
        }
        return shader;
    }
}
