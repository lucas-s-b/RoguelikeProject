package roguelike.content;

import roguelike.definitions.ContentRegistry;
import roguelike.definitions.StatusDefinition;
import roguelike.model.DamageSpec;
import roguelike.model.DamageType;
import roguelike.systems.Attacks;
import roguelike.systems.EffectSystem;

public final class StatusContent {
    public static final String BURN = "burn";
    public static final String POISON = "poison";
    public static final String BLEED = "bleed";
    public static final String CHILL = "chill";
    public static final String FROZEN = "frozen";
    public static final String MARKED = "marked";
    public static final String SHOCKED = "shocked";

    private StatusContent() {
    }

    public static void register() {
        ContentRegistry.add(StatusDefinition.create(BURN)
                .name("Burn")
                .color(1f, 0.5f, 0.1f)
                .maxStacks(10)
                .duration(3f)
                .tickInterval(0.5f)
                .onTick((world, unit, status) ->
                        Attacks.hitUnit(world, status.source, unit, DamageSpec.status(2f * status.stacks, DamageType.FIRE)))
                .visual((world, unit, status, delta) ->
                        EffectSystem.flames(world, unit, status.definition.color, 6f + 3f * status.stacks, delta)));

        ContentRegistry.add(StatusDefinition.create(POISON)
                .name("Poison")
                .color(0.45f, 0.9f, 0.25f)
                .maxStacks(20)
                .duration(6f)
                .tickInterval(0.5f)
                .onTick((world, unit, status) ->
                        Attacks.hitUnit(world, status.source, unit, DamageSpec.status(1f * status.stacks, DamageType.POISON)))
                .visual((world, unit, status, delta) ->
                        EffectSystem.bubbles(world, unit, status.definition.color, 3f + status.stacks, delta)));

        ContentRegistry.add(StatusDefinition.create(BLEED)
                .name("Bleed")
                .color(0.85f, 0.1f, 0.15f)
                .maxStacks(8)
                .duration(2.5f)
                .tickInterval(0.5f)
                .onTick((world, unit, status) ->
                        Attacks.hitUnit(world, status.source, unit, DamageSpec.status(2.5f * status.stacks, DamageType.BLEED)))
                .visual((world, unit, status, delta) ->
                        EffectSystem.drips(world, unit, status.definition.color, 4f + 3f * status.stacks, delta)));

        ContentRegistry.add(StatusDefinition.create(CHILL)
                .name("Chill")
                .color(0.55f, 0.85f, 1f)
                .maxStacks(5)
                .duration(2.5f)
                .slowPercentPerStack(10f)
                .visual((world, unit, status, delta) ->
                        EffectSystem.frost(world, unit, status.definition.color, 4f + 2f * status.stacks, delta)));

        ContentRegistry.add(StatusDefinition.create(FROZEN)
                .name("Frozen")
                .color(0.8f, 0.95f, 1f)
                .maxStacks(1)
                .duration(1f)
                .stuns()
                .visual((world, unit, status, delta) ->
                        EffectSystem.frost(world, unit, status.definition.color, 16f, delta)));

        ContentRegistry.add(StatusDefinition.create(MARKED)
                .name("Marked")
                .color(1f, 0.3f, 0.6f)
                .maxStacks(5)
                .duration(4f)
                .damageTakenPercentPerStack(8f)
                .visual((world, unit, status, delta) ->
                        EffectSystem.sparkles(world, unit, status.definition.color, 3f + 1.5f * status.stacks, delta)));

        ContentRegistry.add(StatusDefinition.create(SHOCKED)
                .name("Shocked")
                .color(1f, 0.95f, 0.4f)
                .maxStacks(1)
                .duration(0.6f)
                .stuns()
                .visual((world, unit, status, delta) ->
                        EffectSystem.sparkles(world, unit, status.definition.color, 12f, delta)));
    }
}
