package io.wispforest.condensed_creative.data;

import com.mojang.logging.LogUtils;
import io.wispforest.condensed_creative.CondensedCreative;
import io.wispforest.condensed_creative.compat.EntryTypeCondensing;
import io.wispforest.condensed_creative.entry.impl.CondensedItemEntry;
import io.wispforest.condensed_creative.registry.CondensedCreativeInitializer;
import io.wispforest.condensed_creative.registry.CondensedEntryRegistry;
import net.minecraft.block.*;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.entity.decoration.painting.PaintingVariant;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.registry.tag.PaintingVariantTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

@CondensedCreativeInitializer.InitializeCondensedEntries
public class BuiltinEntries implements CondensedCreativeInitializer {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final List<Function<String, String>> WOOD_BLOCK_TYPES = List.of(
            (woodType) -> "*_" + ((woodType.equals("crimson") || woodType.equals("warped")) ? "stem" : "log"),
            (woodType) -> "*_" + ((woodType.equals("crimson") || woodType.equals("warped")) ? "hyphae" : "wood"),
            (woodType) -> "stripped_*_" + ((woodType.equals("crimson") || woodType.equals("warped")) ? "stem" : "log"),
            (woodType) -> "stripped_*_" + ((woodType.equals("crimson") || woodType.equals("warped")) ? "hyphae" : "wood"),
            (woodType) -> "*_planks",
            (woodType) -> "*_stairs",
            (woodType) -> "*_slab",
            (woodType) -> "*_fence",
            (woodType) -> "*_fence_gate",
            (woodType) -> "*_door",
            (woodType) -> "*_trapdoor",
            (woodType) -> "*_pressure_plate",
            (woodType) -> "*_button"
    );

    private static final List<SpawnGroup> creatures = List.of(
            SpawnGroup.CREATURE,
            SpawnGroup.AXOLOTLS,
            SpawnGroup.AMBIENT,
            SpawnGroup.WATER_CREATURE,
            SpawnGroup.WATER_AMBIENT,
            SpawnGroup.UNDERGROUND_WATER_CREATURE
    );

    @Override
    public void registerCondensedEntries(boolean refreshed) {
        if(!CondensedCreative.getConfig().defaultCCGroups) return;

//        CondensedEntryRegistry.fromTag(CondensedCreative.createID("logs"), Blocks.OAK_LOG, ItemTags.LOGS)
//                .toggleStrictFiltering(true)
//                .addToItemGroup(ItemGroups.BUILDING_BLOCKS);

        WoodType.stream().forEach(signType -> {
            Identifier identifier = new Identifier(signType.name());

            List<ItemStack> woodItemStacks = new ArrayList<>();

            WOOD_BLOCK_TYPES.forEach(blockType -> {
                Item item = Registries.ITEM.get(new Identifier(identifier.getNamespace(), blockType.apply(identifier.getPath()).replace("*", identifier.getPath())));

                if(item != Items.AIR) woodItemStacks.add(item.getDefaultStack());
            });

            if(woodItemStacks.isEmpty()){
                LOGGER.warn("[CondensedCreative]: Attempted to create a builtin entry for the given WoodType [WoodType: {}, BlockSetType: {}] but was unable to find the registry entries!", signType.name(), signType.setType().name());

                return;
            }

            if(signType == WoodType.BAMBOO){
                woodItemStacks.add(0, Items.BAMBOO_BLOCK.getDefaultStack());
                woodItemStacks.add(1, Items.STRIPPED_BAMBOO_BLOCK.getDefaultStack());

                woodItemStacks.add(5, Items.BAMBOO_MOSAIC.getDefaultStack());
                woodItemStacks.add(6, Items.BAMBOO_MOSAIC_STAIRS.getDefaultStack());
                woodItemStacks.add(7, Items.BAMBOO_MOSAIC_SLAB.getDefaultStack());
            }

            CondensedEntryRegistry.fromItemStacks(identifier, woodItemStacks.get(0), woodItemStacks)
                    .toggleStrictFiltering(true)
                    .setEntryOrder(CondensedItemEntry.EntryOrder.ITEMGROUP_ORDER)
                    .addToItemGroup(ItemGroups.BUILDING_BLOCKS);
        });

        Map<String, Item> stoneTypes = Map.ofEntries(
                Map.entry("stone", Items.STONE),
                Map.entry("granite", Items.GRANITE),
                Map.entry("diorite", Items.DIORITE),
                Map.entry("andesite", Items.ANDESITE),
                Map.entry("sandstone", Items.SANDSTONE),
                Map.entry("prismarine", Items.PRISMARINE),
                Map.entry("deepslate", Items.DEEPSLATE),
                Map.entry("blackstone", Items.BLACKSTONE),
                Map.entry("end_stone", Items.END_STONE),
                Map.entry("quartz", Items.QUARTZ_BLOCK),
                Map.entry("copper", Items.COPPER_BLOCK));

        stoneTypes.forEach((type, startingItem) -> {
            Identifier identifier = new Identifier(type);

            List<ItemStack> stoneItemStacks = new ArrayList<>();

            Registries.BLOCK.getIds().forEach(identifier1 -> {
                String path = identifier1.getPath();

                if(!identifier1.getNamespace().equals("minecraft") || !path.contains(type)) return;

                List<String> listOfMatches = stoneTypes.keySet().stream()
                        .filter((type1) -> path.contains(type1) && !type1.equals(type) && type1.contains(type))
                        .toList();

                if(listOfMatches.isEmpty()) stoneItemStacks.add(Registries.ITEM.get(identifier1).getDefaultStack());
            });

            if (!stoneItemStacks.isEmpty()) {
                CondensedEntryRegistry.fromItemStacks(identifier, startingItem, stoneItemStacks)
                        .toggleStrictFiltering(true)
                        .setEntryOrder(CondensedItemEntry.EntryOrder.ITEMGROUP_ORDER)
                        .addToItemGroup(ItemGroups.BUILDING_BLOCKS);
            } else {
                LOGGER.warn("The given material Type [{}] seems to have not matched anything!", type);
            }
        });


        CondensedEntryRegistry.of(CondensedCreative.createID("signs"), Items.OAK_SIGN, item -> fromTags(item, ItemTags.SIGNS, ItemTags.HANGING_SIGNS))
                .addToItemGroup(ItemGroups.FUNCTIONAL);

        CondensedEntryRegistry.of(CondensedCreative.createID("infested_blocks"), Items.OAK_SIGN, item -> item instanceof BlockItem bi && bi.getBlock() instanceof InfestedBlock)
                .addToItemGroup(ItemGroups.FUNCTIONAL);

        CondensedEntryRegistry.ofSupplier(CondensedCreative.createID("paintings"), Items.PAINTING, () -> {
                    List<ItemStack> paintingVariantStacks = new ArrayList<>();

                    Registries.PAINTING_VARIANT.streamEntries()
                            .filter(pv -> pv.isIn(PaintingVariantTags.PLACEABLE))
                            .sorted(Comparator.comparing(
                                    RegistryEntry::value,
                                    Comparator.<PaintingVariant>comparingInt(pv -> pv.getHeight() * pv.getWidth()).thenComparing(PaintingVariant::getWidth)
                            ))
                            .forEach(paintingVariant -> {
                                ItemStack itemStack = new ItemStack(Items.PAINTING);

                                PaintingEntity.writeVariantToNbt(itemStack.getOrCreateSubNbt("EntityTag"), paintingVariant);

                                paintingVariantStacks.add(itemStack);
                            });

                    return paintingVariantStacks;
                })
                .addToItemGroup(ItemGroups.FUNCTIONAL);

        CondensedEntryRegistry.fromTag(CondensedCreative.createID("wools"), Blocks.WHITE_WOOL, ItemTags.WOOL)
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.COLORED_BLOCKS);

        CondensedEntryRegistry.fromTag(CondensedCreative.createID("terracotta"), Blocks.TERRACOTTA, ItemTags.TERRACOTTA)
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.COLORED_BLOCKS);

        CondensedEntryRegistry.of(CondensedCreative.createID("concrete"), Blocks.WHITE_CONCRETE,
                (item) -> {
                    if(!(item instanceof BlockItem))return false;

                    String itemPath = Registries.ITEM.getId(item).getPath();

                    return itemPath.contains("concrete") && !itemPath.contains("powder");
                })
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.COLORED_BLOCKS);

        CondensedEntryRegistry.of(CondensedCreative.createID("concrete_powder"), Blocks.WHITE_CONCRETE_POWDER,
                (item) -> {
                    if(!(item instanceof BlockItem)) return false;

                    String itemPath = Registries.ITEM.getId(item).getPath();

                    return itemPath.contains("concrete") && itemPath.contains("powder");
                })
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.COLORED_BLOCKS);

        CondensedEntryRegistry.fromItems(CondensedCreative.createID("ores"), Blocks.IRON_ORE,
                Stream.of(Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE,
                        Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE,
                        Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE,
                        Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE,
                        Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE,
                        Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
                        Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE,
                        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
                        Blocks.NETHER_GOLD_ORE, Blocks.NETHER_QUARTZ_ORE
                ).map(Block::asItem).toList())
                .addToItemGroup(ItemGroups.NATURAL);

        CondensedEntryRegistry.of(CondensedCreative.createID("glass"), Blocks.WHITE_STAINED_GLASS,
                (item) -> {
                    var path = Registries.ITEM.getId(item).getPath();

                    return path.contains("glass") && !path.contains("pane");
                })
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.COLORED_BLOCKS);

        //-------------------------------

        CondensedEntryRegistry.fromTag(CondensedCreative.createID("carpets"), Blocks.WHITE_CARPET, ItemTags.WOOL_CARPETS)
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.COLORED_BLOCKS);


        CondensedEntryRegistry.fromTag(CondensedCreative.createID("candles"), Blocks.WHITE_CANDLE, ItemTags.CANDLES)
                .toggleStrictFiltering(true)
                .addToItemGroups(ItemGroups.COLORED_BLOCKS, ItemGroups.FUNCTIONAL);


        CondensedEntryRegistry.fromTag(CondensedCreative.createID("beds"), Blocks.WHITE_BED, ItemTags.BEDS)
                .toggleStrictFiltering(true)
                .addToItemGroups(ItemGroups.COLORED_BLOCKS, ItemGroups.FUNCTIONAL);


        CondensedEntryRegistry.fromTag(CondensedCreative.createID("banners"), Blocks.WHITE_BANNER, ItemTags.BANNERS)
                .toggleStrictFiltering(true)
                .addToItemGroups(ItemGroups.COLORED_BLOCKS, ItemGroups.FUNCTIONAL);


//        CondensedEntryRegistry.fromTag(CondensedCreative.createID("walls"), Blocks.COBBLESTONE_WALL, ItemTags.WALLS)
//                .toggleStrictFiltering(true)
//                .addToItemGroup(ItemGroups.BUILDING_BLOCKS);


//        CondensedEntryRegistry.fromTag(CondensedCreative.createID("fences"), Blocks.OAK_FENCE, ItemTags.FENCES)
//                .toggleStrictFiltering(true)
//                .addToItemGroup(ItemGroups.BUILDING_BLOCKS);


        CondensedEntryRegistry.of(CondensedCreative.createID("glass_panes"), Blocks.GLASS_PANE,
                (item) -> {
                    var path = Registries.ITEM.getId(item).getPath();

                    return path.contains("glass") && path.contains("pane");
                })
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.COLORED_BLOCKS);


        CondensedEntryRegistry.of(CondensedCreative.createID("corals"), Blocks.BRAIN_CORAL,
                predicateWithVanillaCheck((item) -> item instanceof BlockItem blockItem && blockItem.getBlock() instanceof CoralParentBlock))
                .addToItemGroup(ItemGroups.NATURAL);


        CondensedEntryRegistry.of(CondensedCreative.createID("glazed_terracotta"), Blocks.WHITE_GLAZED_TERRACOTTA,
                        predicateWithVanillaCheck((item) -> item instanceof BlockItem blockItem && blockItem.getBlock() instanceof GlazedTerracottaBlock))
                .addToItemGroup(ItemGroups.COLORED_BLOCKS);


        CondensedEntryRegistry.fromTag(CondensedCreative.createID("shulkers"), Blocks.SHULKER_BOX, BlockTags.SHULKER_BOXES)
                .toggleStrictFiltering(true)
                .addToItemGroups(ItemGroups.COLORED_BLOCKS, ItemGroups.FUNCTIONAL);

        //-------------------------------

        CondensedEntryRegistry.fromTag(CondensedCreative.createID("buttons"), Blocks.STONE_BUTTON, ItemTags.BUTTONS)
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.REDSTONE);


        CondensedEntryRegistry.fromTag(CondensedCreative.createID("pressure_plates"), Blocks.STONE_PRESSURE_PLATE, BlockTags.PRESSURE_PLATES)
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.REDSTONE);


        CondensedEntryRegistry.fromTag(CondensedCreative.createID("doors"), Blocks.IRON_DOOR, BlockTags.DOORS)
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.REDSTONE);


        CondensedEntryRegistry.fromTag(CondensedCreative.createID("trapdoors"), Blocks.IRON_TRAPDOOR, BlockTags.TRAPDOORS)
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.REDSTONE);


        CondensedEntryRegistry.fromTag(CondensedCreative.createID("fence_gates"), Blocks.OAK_FENCE_GATE, BlockTags.FENCE_GATES)
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.REDSTONE);

        //-------------------------------

        CondensedEntryRegistry.of(CondensedCreative.createID(SpawnGroup.CREATURE.toString().toLowerCase()), Items.BEE_SPAWN_EGG,
                        isSpawnEggItem(spawnEggItem -> creatures.contains(spawnEggItem.getEntityType(null).getSpawnGroup())))
                .addToItemGroup(ItemGroups.SPAWN_EGGS);

        CondensedEntryRegistry.of(CondensedCreative.createID(SpawnGroup.MONSTER.toString().toLowerCase()), Items.ZOMBIE_SPAWN_EGG,
                        isSpawnEggItem(spawnEggItem -> spawnEggItem.getEntityType(null).getSpawnGroup() == SpawnGroup.MONSTER))
                .addToItemGroup(ItemGroups.SPAWN_EGGS);

        CondensedEntryRegistry.of(CondensedCreative.createID(SpawnGroup.MISC.toString().toLowerCase()), Items.VILLAGER_SPAWN_EGG,
                        isSpawnEggItem(spawnEggItem -> spawnEggItem.getEntityType(null).getSpawnGroup() == SpawnGroup.MISC))
                .addToItemGroup(ItemGroups.SPAWN_EGGS);

//        CondensedEntryRegistry.of(CondensedCreative.createID("spawn_eggs"), Items.AXOLOTL_SPAWN_EGG, item -> item instanceof SpawnEggItem)
//                .addToItemGroup(ItemGroups.SPAWN_EGGS);


        CondensedEntryRegistry.fromTag(CondensedCreative.createID("music_discs"), Items.MUSIC_DISC_13, ItemTags.MUSIC_DISCS)
                .addToItemGroup(ItemGroups.TOOLS);

        CondensedEntryRegistry.fromTag(CondensedCreative.createID("boats"), Items.OAK_BOAT, ItemTags.BOATS)
                .toggleStrictFiltering(true)
                .addToItemGroup(ItemGroups.TOOLS);

        ItemGroup combat = Registries.ITEM_GROUP.get(ItemGroups.COMBAT);

        addPotionBasedEntries(Items.TIPPED_ARROW, combat, 0, "Arrows", CondensedCreative.getConfig().defaultEntriesConfig.tippedArrows);

        EntryTypeCondensing potion = CondensedCreative.getConfig().defaultEntriesConfig.potions;

        {
            Set<ItemStack> stacks = ItemStackSet.create();

            for (SuspiciousStewIngredient susStewIngr : SuspiciousStewIngredient.getAll()) {
                stacks.add(Util.make(new ItemStack(Items.SUSPICIOUS_STEW), stack -> {
                    SuspiciousStewItem.addEffectsToStew(stack, susStewIngr.getStewEffects());
                }));
            }

            CondensedEntryRegistry.fromItemStacks(CondensedCreative.createID("suspicious_stews"), stacks.iterator().next(), stacks)
                    .addToItemGroup(ItemGroups.FOOD_AND_DRINK);
        }

        ItemGroup foodAndDrink = Registries.ITEM_GROUP.get(ItemGroups.FOOD_AND_DRINK);

        addPotionBasedEntries(Items.POTION, foodAndDrink, 0, "Potions", potion);
        addPotionBasedEntries(Items.SPLASH_POTION, foodAndDrink, 1, "Potions", potion);
        addPotionBasedEntries(Items.LINGERING_POTION, foodAndDrink, 1, "Potions", potion);

        //-------------------------------

        CondensedEntryRegistry.fromItems(CondensedCreative.createID("dyes"), Items.WHITE_DYE,
                Arrays.stream(DyeColor.values())
                        .map(dyeColor -> Registries.ITEM.get(new Identifier(dyeColor.getName() + "_dye")))
                        .filter(item -> item != Items.AIR)
                        .toList()
        ).addToItemGroup(ItemGroups.INGREDIENTS);

        addEnchantmentEntries();

        CondensedEntryRegistry.fromTag(CondensedCreative.createID("pottery_sherds"), Items.ANGLER_POTTERY_SHERD, ItemTags.DECORATED_POT_SHERDS)
                .addToItemGroup(ItemGroups.INGREDIENTS);

        CondensedEntryRegistry.of(CondensedCreative.createID("templates"), Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, i -> i instanceof SmithingTemplateItem)
                .addToItemGroup(ItemGroups.INGREDIENTS);

        //---------------------
    }

    private static void addEnchantmentEntries(){
        EntryTypeCondensing entryTypeCondensing = CondensedCreative.getConfig().defaultEntriesConfig.enchantmentBooks;

        if(entryTypeCondensing == EntryTypeCondensing.NONE) return;

        Set<EnchantmentTarget> targets = EnumSet.allOf(EnchantmentTarget.class);

        List<ItemStack> allEnchantmentBooks = new ArrayList<>();

        for (Enchantment enchantment : Registries.ENCHANTMENT) {
            if (!targets.contains(enchantment.target)) continue;

            List<ItemStack> enchantmentBooks = new ArrayList<>();

            for (int i = enchantment.getMinLevel(); i <= enchantment.getMaxLevel(); ++i) {
                enchantmentBooks.add(EnchantedBookItem.forEnchantment(new EnchantmentLevelEntry(enchantment, i)));
            }

            if(entryTypeCondensing == EntryTypeCondensing.SEPARATE_COLLECTIONS){
                MutableText mutableText = Text.translatable(enchantment.getTranslationKey());

                mutableText.formatted(Formatting.WHITE);

                CondensedEntryRegistry.fromItemStacks(CondensedCreative.createID(enchantment.getTranslationKey()), enchantmentBooks.get(0), enchantmentBooks)
                        .setTitle(mutableText)
                        .addToItemGroup(ItemGroups.INGREDIENTS);
            } else {
                allEnchantmentBooks.addAll(enchantmentBooks);
            }
        }

        if(!allEnchantmentBooks.isEmpty()){
            CondensedEntryRegistry.fromItemStacks(CondensedCreative.createID("enchantment_books"), Items.ENCHANTED_BOOK, allEnchantmentBooks)
                    .addToItemGroup(ItemGroups.INGREDIENTS);
        }
    }

    private static void addPotionBasedEntries(Item potionBasedItem, ItemGroup targetGroup, int wordIndex, String pluralizedWord, EntryTypeCondensing entryTypeCondensing){
        if(entryTypeCondensing == EntryTypeCondensing.NONE) return;

        Map<List<StatusEffect>, List<Potion>> sortedPotions = new LinkedHashMap<>();

        for (Potion potion : Registries.POTION) {
            if (potion == Potions.EMPTY) continue;

            List<StatusEffect> effects = potion.getEffects().stream()
                    .map(StatusEffectInstance::getEffectType).toList();

            sortedPotions.computeIfAbsent(effects, statusEffects -> new ArrayList<>()).add(potion);
        }

        List<ItemStack> allPotionItems = new ArrayList<>();

        sortedPotions.forEach((statusEffects, potions) -> {
            List<ItemStack> potionItems = new ArrayList<>();

            potions.forEach(p -> potionItems.add(PotionUtil.setPotion(new ItemStack(potionBasedItem), p)));

            if (potionItems.isEmpty()) return;

            if(entryTypeCondensing == EntryTypeCondensing.SEPARATE_COLLECTIONS) {
                String translationKey = potionItems.get(0).getTranslationKey();

                CondensedEntryRegistry.fromItemStacks(CondensedCreative.createID(translationKey), potionItems.get(0), potionItems)
                        .setTitleSupplier(() -> {
                            String[] words = Text.translatable(translationKey).getString()
                                    .split(" ");

                            words[wordIndex] = pluralizedWord;

                            return Text.literal(StringUtils.join(words, " "))
                                    .formatted(Formatting.WHITE);
                        })
                        .addToItemGroup(targetGroup);
            } else {
                allPotionItems.addAll(potionItems);
            }
        });

        if(!allPotionItems.isEmpty()){
            Identifier itemId = Registries.ITEM.getId(potionBasedItem);

            String[] words = itemId.getPath().split("_");

            words[words.length - 1] = pluralizedWord.toLowerCase();

            String path = StringUtils.join(words, "_");

            CondensedEntryRegistry.fromItemStacks(CondensedCreative.createID(path), potionBasedItem, allPotionItems)
                    .addToItemGroup(targetGroup);
        }
    }

    private static final Predicate<Item> vanillaItemsOnly = item -> Objects.equals(Registries.ITEM.getId(item).getNamespace(), "minecraft");

    private static Predicate<Item> predicateWithVanillaCheck(Predicate<Item> mainPredicate){
        return (item) -> BuiltinEntries.vanillaItemsOnly.and(mainPredicate).test(item);
    }

    private static Predicate<Item> isSpawnEggItem(Predicate<SpawnEggItem> predicate){
        return (item) -> item instanceof SpawnEggItem spawnEggItem && predicate.test(spawnEggItem);
    }

    @SafeVarargs
    private static <T extends ItemConvertible> boolean fromTags(Item item, TagKey<T> ...tagKeys){
        if(tagKeys.length == 0) return false;

        boolean isInTag = false;

        for(TagKey<T> tagKey: tagKeys) {
            if (tagKey.isOf(RegistryKeys.ITEM)) {
                isInTag = item.getRegistryEntry().isIn((TagKey<Item>) tagKey);
            } else if (tagKey.isOf(RegistryKeys.BLOCK)) {
                isInTag = item instanceof BlockItem blockItem && blockItem.getBlock().getRegistryEntry().isIn((TagKey<Block>) tagKey);
            } else {
                LOGGER.warn("It seems that a Condensed Entry was somehow registered with Tag that isn't part of the Item or Block Registry");
            }

            if(isInTag) return true;
        }

        return isInTag;
    }
}
