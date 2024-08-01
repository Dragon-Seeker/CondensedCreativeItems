package io.wispforest.condensed_creative.data;

import com.mojang.logging.LogUtils;
import io.wispforest.condensed_creative.CondensedCreative;
import io.wispforest.condensed_creative.compat.EntryTypeCondensing;
import io.wispforest.condensed_creative.entry.impl.CondensedItemEntry;
import io.wispforest.condensed_creative.registry.CondensedCreativeInitializer;
import io.wispforest.condensed_creative.registry.CondensedEntryRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.*;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.decoration.PaintingVariant;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.IntStream;
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

    private static final List<MobCategory> creatures = List.of(
            MobCategory.CREATURE,
            MobCategory.AXOLOTLS,
            MobCategory.AMBIENT,
            MobCategory.WATER_CREATURE,
            MobCategory.WATER_AMBIENT,
            MobCategory.UNDERGROUND_WATER_CREATURE
    );

    @Override
    public void registerCondensedEntries(boolean refreshed, RegistryAccess registryAccess) {
        if(!CondensedCreative.getConfig().defaultCCGroups) return;

//        CondensedEntryRegistry.fromTag(CondensedCreative.createID("logs"), Blocks.OAK_LOG, ItemTags.LOGS)
//                .toggleStrictFiltering(true)
//                .addToItemGroup(ItemGroups.BUILDING_BLOCKS);

        WoodType.values().forEach(signType -> {
            ResourceLocation identifier = ResourceLocation.parse(signType.name());

            List<ItemStack> woodItemStacks = new ArrayList<>();

            WOOD_BLOCK_TYPES.forEach(blockType -> {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(identifier.getNamespace(), blockType.apply(identifier.getPath()).replace("*", identifier.getPath())));

                if(item != Items.AIR) woodItemStacks.add(item.getDefaultInstance());
            });

            if(woodItemStacks.isEmpty()){
                LOGGER.warn("[CondensedCreative]: Attempted to create a builtin entry for the given WoodType [WoodType: {}, BlockSetType: {}] but was unable to find the registry entries!", signType.name(), signType.setType().name());

                return;
            }

            if(signType == WoodType.BAMBOO){
                woodItemStacks.add(0, Items.BAMBOO_BLOCK.getDefaultInstance());
                woodItemStacks.add(1, Items.STRIPPED_BAMBOO_BLOCK.getDefaultInstance());

                woodItemStacks.add(5, Items.BAMBOO_MOSAIC.getDefaultInstance());
                woodItemStacks.add(6, Items.BAMBOO_MOSAIC_STAIRS.getDefaultInstance());
                woodItemStacks.add(7, Items.BAMBOO_MOSAIC_SLAB.getDefaultInstance());
            }

            CondensedEntryRegistry.fromItemStacks(identifier, woodItemStacks.get(0), woodItemStacks)
                    .toggleStrictFiltering(true)
                    .setEntryOrder(CondensedItemEntry.EntryOrder.ITEMGROUP_ORDER)
                    .addToItemGroup(CreativeModeTabs.BUILDING_BLOCKS);
        });

        Map<String, Item> stoneTypes = Map.ofEntries(
                Map.entry("stone", Items.STONE),
                Map.entry("granite", Items.GRANITE),
                Map.entry("diorite", Items.DIORITE),
                Map.entry("andesite", Items.ANDESITE),
                Map.entry("tuff", Items.TUFF),
                //Map.entry("brick", Items.BRICKS),
                Map.entry("mud", Items.PACKED_MUD),
                Map.entry("sandstone", Items.SANDSTONE),
                Map.entry("red_sandstone", Items.RED_SANDSTONE),
                Map.entry("prismarine", Items.PRISMARINE),
                Map.entry("deepslate", Items.DEEPSLATE),
                Map.entry("nether_brick", Items.NETHER_BRICK),
                Map.entry("blackstone", Items.BLACKSTONE),
                Map.entry("end_stone", Items.END_STONE),
                Map.entry("quartz", Items.QUARTZ_BLOCK),
                Map.entry("copper", Items.COPPER_BLOCK));

        stoneTypes.forEach((type, startingItem) -> {
            ResourceLocation identifier = ResourceLocation.parse(type);

            List<ItemStack> stoneItemStacks = new ArrayList<>();

            BuiltInRegistries.BLOCK.keySet().forEach(identifier1 -> {
                String path = identifier1.getPath();

                if(!identifier1.getNamespace().equals("minecraft") || !path.contains(type)) return;

                List<String> listOfMatches = stoneTypes.keySet().stream()
                        .filter((type1) -> path.contains(type1) && !type1.equals(type) && type1.contains(type))
                        .toList();

                if(listOfMatches.isEmpty()) stoneItemStacks.add(BuiltInRegistries.ITEM.get(identifier1).getDefaultInstance());
            });

            if (!stoneItemStacks.isEmpty()) {
                CondensedEntryRegistry.fromItemStacks(identifier, startingItem, stoneItemStacks)
                        .toggleStrictFiltering(true)
                        .setEntryOrder(CondensedItemEntry.EntryOrder.ITEMGROUP_ORDER)
                        .addToItemGroup(CreativeModeTabs.BUILDING_BLOCKS);
            } else {
                LOGGER.warn("The given material Type [{}] seems to have not matched anything!", type);
            }
        });


        CondensedEntryRegistry.of(CondensedCreative.location("signs"), Items.OAK_SIGN, item -> fromTags(item, ItemTags.SIGNS, ItemTags.HANGING_SIGNS))
                .addToItemGroup(CreativeModeTabs.FUNCTIONAL_BLOCKS);

        CondensedEntryRegistry.of(CondensedCreative.location("infested_blocks"), Items.OAK_SIGN, item -> item instanceof BlockItem bi && bi.getBlock() instanceof InfestedBlock)
                .addToItemGroup(CreativeModeTabs.FUNCTIONAL_BLOCKS);

        CondensedEntryRegistry.fromTag(CondensedCreative.location("wools"), Blocks.WHITE_WOOL, ItemTags.WOOL)
                .toggleStrictFiltering(true)
                .addToItemGroup(CreativeModeTabs.COLORED_BLOCKS);

        CondensedEntryRegistry.fromTag(CondensedCreative.location("terracotta"), Blocks.TERRACOTTA, ItemTags.TERRACOTTA)
                .toggleStrictFiltering(true)
                .addToItemGroup(CreativeModeTabs.COLORED_BLOCKS);

        CondensedEntryRegistry.of(CondensedCreative.location("concrete"), Blocks.WHITE_CONCRETE,
                (item) -> {
                    if(!(item instanceof BlockItem))return false;

                    String itemPath = BuiltInRegistries.ITEM.getKey(item).getPath();

                    return itemPath.contains("concrete") && !itemPath.contains("powder");
                })
                .toggleStrictFiltering(true)
                .addToItemGroup(CreativeModeTabs.COLORED_BLOCKS);

        CondensedEntryRegistry.of(CondensedCreative.location("concrete_powder"), Blocks.WHITE_CONCRETE_POWDER,
                (item) -> {
                    if(!(item instanceof BlockItem)) return false;

                    String itemPath = BuiltInRegistries.ITEM.getKey(item).getPath();

                    return itemPath.contains("concrete") && itemPath.contains("powder");
                })
                .toggleStrictFiltering(true)
                .addToItemGroup(CreativeModeTabs.COLORED_BLOCKS);

        CondensedEntryRegistry.fromItems(CondensedCreative.location("ores"), Blocks.IRON_ORE,
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
                .addToItemGroup(CreativeModeTabs.NATURAL_BLOCKS);

//        CondensedEntryRegistry.fromTag(CondensedCreative.location("dirt"), Blocks.DIRT, ItemTags.DIRT)
//                .addToItemGroup(CreativeModeTabs.NATURAL_BLOCKS);

        CondensedEntryRegistry.fromTag(CondensedCreative.location("flowers"), Blocks.DANDELION, BlockTags.FLOWERS)
                .addToItemGroup(CreativeModeTabs.NATURAL_BLOCKS);

        CondensedEntryRegistry.fromTag(CondensedCreative.location("saplings"), Blocks.OAK_SAPLING, BlockTags.SAPLINGS)
                .addToItemGroup(CreativeModeTabs.NATURAL_BLOCKS);

        CondensedEntryRegistry.fromTag(CondensedCreative.location("leaves"), Blocks.OAK_LEAVES, BlockTags.LEAVES)
                .addToItemGroup(CreativeModeTabs.NATURAL_BLOCKS);

        CondensedEntryRegistry.ofSupplier(CondensedCreative.location("logs"), Blocks.OAK_LOG, () -> {
                    var blockLookup = registryAccess.lookupOrThrow(Registries.BLOCK);

                    return blockLookup.get(BlockTags.LOGS)
                            .map(HolderSet.ListBacked::stream)
                            .orElse(Stream.of())
                            .filter(blockHolder -> {
                                var path = blockHolder.unwrapKey().get().location().getPath();

                                return !path.contains("stripped") && (path.contains("log") || path.contains("stem"));
                            })
                            .map(blockHolder -> blockHolder.value().asItem().getDefaultInstance()).toList();
                })
                .addToItemGroup(CreativeModeTabs.NATURAL_BLOCKS);

        CondensedEntryRegistry.of(CondensedCreative.location("glass"), Blocks.WHITE_STAINED_GLASS,
                (item) -> {
                    var path = BuiltInRegistries.ITEM.getKey(item).getPath();

                    return path.contains("glass") && !path.contains("pane");
                })
                .toggleStrictFiltering(true)
                .addToItemGroup(CreativeModeTabs.COLORED_BLOCKS);

        //-------------------------------

        CondensedEntryRegistry.fromTag(CondensedCreative.location("carpets"), Blocks.WHITE_CARPET, ItemTags.WOOL_CARPETS)
                .toggleStrictFiltering(true)
                .addToItemGroup(CreativeModeTabs.COLORED_BLOCKS);


        CondensedEntryRegistry.fromTag(CondensedCreative.location("candles"), Blocks.WHITE_CANDLE, ItemTags.CANDLES)
                .toggleStrictFiltering(true)
                .addToItemGroups(CreativeModeTabs.COLORED_BLOCKS, CreativeModeTabs.FUNCTIONAL_BLOCKS);


        CondensedEntryRegistry.fromTag(CondensedCreative.location("beds"), Blocks.WHITE_BED, ItemTags.BEDS)
                .toggleStrictFiltering(true)
                .addToItemGroups(CreativeModeTabs.COLORED_BLOCKS, CreativeModeTabs.FUNCTIONAL_BLOCKS);


        CondensedEntryRegistry.fromTag(CondensedCreative.location("banners"), Blocks.WHITE_BANNER, ItemTags.BANNERS)
                .toggleStrictFiltering(true)
                .addToItemGroups(CreativeModeTabs.COLORED_BLOCKS, CreativeModeTabs.FUNCTIONAL_BLOCKS);

        CondensedEntryRegistry.of(CondensedCreative.location("copper_bulbs"), Blocks.COPPER_BULB, item -> {
                    return (item instanceof BlockItem blockItem && blockItem.getBlock() instanceof CopperBulbBlock);
                })
                .toggleStrictFiltering(true)
                .addToItemGroups(CreativeModeTabs.FUNCTIONAL_BLOCKS);

        CondensedEntryRegistry.of(CondensedCreative.location("glass_panes"), Blocks.GLASS_PANE,
                (item) -> {
                    var path = BuiltInRegistries.ITEM.getKey(item).getPath();

                    return path.contains("glass") && path.contains("pane");
                })
                .toggleStrictFiltering(true)
                .addToItemGroup(CreativeModeTabs.COLORED_BLOCKS);


        CondensedEntryRegistry.of(CondensedCreative.location("corals"), Blocks.BRAIN_CORAL,
                predicateWithVanillaCheck((item) -> item instanceof BlockItem blockItem && blockItem.getBlock() instanceof BaseCoralPlantTypeBlock))
                .addToItemGroup(CreativeModeTabs.NATURAL_BLOCKS);


        CondensedEntryRegistry.of(CondensedCreative.location("glazed_terracotta"), Blocks.WHITE_GLAZED_TERRACOTTA,
                        predicateWithVanillaCheck((item) -> item instanceof BlockItem blockItem && blockItem.getBlock() instanceof GlazedTerracottaBlock))
                .addToItemGroup(CreativeModeTabs.COLORED_BLOCKS);


        CondensedEntryRegistry.fromTag(CondensedCreative.location("shulkers"), Blocks.SHULKER_BOX, BlockTags.SHULKER_BOXES)
                .toggleStrictFiltering(true)
                .addToItemGroups(CreativeModeTabs.COLORED_BLOCKS, CreativeModeTabs.FUNCTIONAL_BLOCKS);

        //-------------------------------

        CondensedEntryRegistry.fromTag(CondensedCreative.location("buttons"), Blocks.STONE_BUTTON, ItemTags.BUTTONS)
                .toggleStrictFiltering(true)
                .addToItemGroup(CreativeModeTabs.REDSTONE_BLOCKS);


        CondensedEntryRegistry.fromTag(CondensedCreative.location("pressure_plates"), Blocks.STONE_PRESSURE_PLATE, BlockTags.PRESSURE_PLATES)
                .toggleStrictFiltering(true)
                .addToItemGroup(CreativeModeTabs.REDSTONE_BLOCKS);


        CondensedEntryRegistry.fromTag(CondensedCreative.location("doors"), Blocks.IRON_DOOR, BlockTags.DOORS)
                .toggleStrictFiltering(false)
                .addToItemGroup(CreativeModeTabs.REDSTONE_BLOCKS);


        CondensedEntryRegistry.fromTag(CondensedCreative.location("trapdoors"), Blocks.IRON_TRAPDOOR, BlockTags.TRAPDOORS)
                .toggleStrictFiltering(false)
                .addToItemGroup(CreativeModeTabs.REDSTONE_BLOCKS);

        CondensedEntryRegistry.fromTag(CondensedCreative.location("fence_gates"), Blocks.OAK_FENCE_GATE, BlockTags.FENCE_GATES)
                .toggleStrictFiltering(false)
                .addToItemGroup(CreativeModeTabs.REDSTONE_BLOCKS);

        //-------------------------------

        CondensedEntryRegistry.of(CondensedCreative.location(MobCategory.CREATURE.toString().toLowerCase()), Items.BEE_SPAWN_EGG,
                        isSpawnEggItem(spawnEggItem -> creatures.contains(spawnEggItem.getType(ItemStack.EMPTY).getCategory())))
                .addToItemGroup(CreativeModeTabs.SPAWN_EGGS);

        CondensedEntryRegistry.of(CondensedCreative.location(MobCategory.MONSTER.toString().toLowerCase()), Items.ZOMBIE_SPAWN_EGG,
                        isSpawnEggItem(spawnEggItem -> spawnEggItem.getType(ItemStack.EMPTY).getCategory() == MobCategory.MONSTER))
                .addToItemGroup(CreativeModeTabs.SPAWN_EGGS);

        CondensedEntryRegistry.of(CondensedCreative.location(MobCategory.MISC.toString().toLowerCase()), Items.VILLAGER_SPAWN_EGG,
                        isSpawnEggItem(spawnEggItem -> spawnEggItem.getType(ItemStack.EMPTY).getCategory() == MobCategory.MISC))
                .addToItemGroup(CreativeModeTabs.SPAWN_EGGS);

//        CondensedEntryRegistry.of(CondensedCreative.createID("spawn_eggs"), Items.AXOLOTL_SPAWN_EGG, item -> item instanceof SpawnEggItem)
//                .addToItemGroup(ItemGroups.SPAWN_EGGS);


        CondensedEntryRegistry.of(CondensedCreative.location("music_discs"), Items.MUSIC_DISC_13, (item) -> item.components().has(DataComponents.JUKEBOX_PLAYABLE))
                .addToItemGroup(CreativeModeTabs.TOOLS_AND_UTILITIES);

        CondensedEntryRegistry.fromTag(CondensedCreative.location("boats"), Items.OAK_BOAT, ItemTags.BOATS)
                .toggleStrictFiltering(true)
                .addToItemGroup(CreativeModeTabs.TOOLS_AND_UTILITIES);

        addInstrumentEntries(registryAccess, Items.GOAT_HORN, InstrumentTags.GOAT_HORNS);

        CreativeModeTab combat = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.COMBAT);

        addPotionBasedEntries(Items.TIPPED_ARROW, combat, 0, "Arrows", CondensedCreative.getConfig().defaultEntriesConfig.tippedArrows);

        EntryTypeCondensing potion = CondensedCreative.getConfig().defaultEntriesConfig.potions;

        {
            Set<ItemStack> stacks = ItemStackLinkedSet.createTypeAndComponentsSet();

            for (SuspiciousEffectHolder susStewIngr : SuspiciousEffectHolder.getAllEffectHolders()) {
                stacks.add(Util.make(new ItemStack(Items.SUSPICIOUS_STEW), stack -> {
                    stack.set(DataComponents.SUSPICIOUS_STEW_EFFECTS, susStewIngr.getSuspiciousEffects());
                }));
            }

            CondensedEntryRegistry.fromItemStacks(CondensedCreative.location("suspicious_stews"), stacks.iterator().next(), stacks)
                    .addToItemGroup(CreativeModeTabs.FOOD_AND_DRINKS);
        }

        CreativeModeTab foodAndDrink = BuiltInRegistries.CREATIVE_MODE_TAB.get(CreativeModeTabs.FOOD_AND_DRINKS);

        addPotionBasedEntries(Items.POTION, foodAndDrink, 0, "Potions", potion);
        addPotionBasedEntries(Items.SPLASH_POTION, foodAndDrink, 1, "Potions", potion);
        addPotionBasedEntries(Items.LINGERING_POTION, foodAndDrink, 1, "Potions", potion);

        //-------------------------------

        CondensedEntryRegistry.fromItems(CondensedCreative.location("dyes"), Items.WHITE_DYE,
                Arrays.stream(DyeColor.values())
                        .map(dyeColor -> BuiltInRegistries.ITEM.get(ResourceLocation.parse(dyeColor.getName() + "_dye")))
                        .filter(item -> item != Items.AIR)
                        .toList()
        ).addToItemGroup(CreativeModeTabs.INGREDIENTS);

        addEnchantmentEntries(registryAccess);

        addPaintingEntries(registryAccess);

        CondensedEntryRegistry.fromTag(CondensedCreative.location("pottery_sherds"), Items.ANGLER_POTTERY_SHERD, ItemTags.DECORATED_POT_SHERDS)
                .addToItemGroup(CreativeModeTabs.INGREDIENTS);

        CondensedEntryRegistry.of(CondensedCreative.location("templates"), Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, i -> i instanceof SmithingTemplateItem)
                .addToItemGroup(CreativeModeTabs.INGREDIENTS);

        //---------------------
    }

    private static void addPaintingEntries(RegistryAccess registryAccess) {
        RegistryOps<Tag> registryOps = registryAccess.createSerializationContext(NbtOps.INSTANCE);

        List<ItemStack> paintingVariantStacks = new ArrayList<>();

        registryAccess.lookupOrThrow(Registries.PAINTING_VARIANT).listElements()
                .filter(holder -> holder.is(PaintingVariantTags.PLACEABLE))
                .sorted(Comparator.comparing(Holder::value, Comparator.comparingInt(PaintingVariant::area).thenComparing(PaintingVariant::width)))
                .forEach((reference) -> {
                    CustomData customData = CustomData.EMPTY.update(registryOps, Painting.VARIANT_MAP_CODEC, reference)
                            .getOrThrow()
                            .update((compoundTag) -> compoundTag.putString("id", "minecraft:painting"));

                    var itemStack = new ItemStack(Items.PAINTING);
                    itemStack.set(DataComponents.ENTITY_DATA, customData);
                    paintingVariantStacks.add(itemStack);
                });

        CondensedEntryRegistry.fromItemStacks(CondensedCreative.location("paintings"), Items.PAINTING, paintingVariantStacks).addToItemGroup(CreativeModeTabs.FUNCTIONAL_BLOCKS);
    }

    private static void addInstrumentEntries(RegistryAccess registryAccess, Item item, TagKey<Instrument> instrumentTag) {
        var instrumentLookup = registryAccess.lookupOrThrow(Registries.INSTRUMENT);

        List<ItemStack> instrumentStacks = instrumentLookup.get(instrumentTag)
                .map(tagHolderSet -> tagHolderSet.stream().map(instrument -> InstrumentItem.create(item, instrument)).toList())
                .orElse(List.of());

        if(instrumentStacks.isEmpty()) return;

        CondensedEntryRegistry.fromItemStacks(CondensedCreative.location(instrumentTag.location().getPath()), instrumentStacks.getFirst(), instrumentStacks)
                .toggleStrictFiltering(true)
                .addToItemGroup(CreativeModeTabs.TOOLS_AND_UTILITIES);
    }

    private static void addEnchantmentEntries(RegistryAccess registryAccess) {
        EntryTypeCondensing entryTypeCondensing = CondensedCreative.getConfig().defaultEntriesConfig.enchantmentBooks;

        if(entryTypeCondensing == EntryTypeCondensing.NONE) return;

        List<ItemStack> allEnchantmentBooks = new ArrayList<>();

        registryAccess.lookupOrThrow(Registries.ENCHANTMENT).listElements().forEach((reference) -> {
            var enchantment = reference.value();

            var enchantmentBooks = IntStream.rangeClosed(enchantment.getMinLevel(), enchantment.getMaxLevel())
                    .mapToObj((i) -> EnchantedBookItem.createForEnchantment(new EnchantmentInstance(reference, i)))
                    .toList();

            if(entryTypeCondensing == EntryTypeCondensing.SEPARATE_COLLECTIONS){
                MutableComponent mutableText = Component.empty().append(enchantment.description());

                mutableText.withStyle(ChatFormatting.WHITE);

                CondensedEntryRegistry.fromItemStacks(CondensedCreative.location(reference.key().location().getPath()), enchantmentBooks.getFirst(), enchantmentBooks)
                        .setTitle(mutableText)
                        .addToItemGroup(CreativeModeTabs.INGREDIENTS);
            } else {
                allEnchantmentBooks.addAll(enchantmentBooks);
            }
        });

        if(!allEnchantmentBooks.isEmpty()){
            CondensedEntryRegistry.fromItemStacks(CondensedCreative.location("enchantment_books"), Items.ENCHANTED_BOOK, allEnchantmentBooks)
                    .addToItemGroup(CreativeModeTabs.INGREDIENTS);
        }
    }

    private static void addPotionBasedEntries(Item potionBasedItem, CreativeModeTab targetGroup, int wordIndex, String pluralizedWord, EntryTypeCondensing entryTypeCondensing){
        if(entryTypeCondensing == EntryTypeCondensing.NONE) return;

        Map<List<MobEffect>, List<Holder<Potion>>> sortedPotions = new LinkedHashMap<>();

        BuiltInRegistries.POTION.holders().forEach(potion -> {
            //if (potion.value() == Potions.WATER.value()) return;

            List<MobEffect> effects = potion.value().getEffects().stream()
                    .map(MobEffectInstance::getEffect)
                    .map(Holder::value)
                    .toList();

            sortedPotions.computeIfAbsent(effects, statusEffects -> new ArrayList<>()).add(potion);
        });

        List<ItemStack> allPotionItems = new ArrayList<>();

        for (var potions : sortedPotions.values()) {
            List<ItemStack> potionItems = new ArrayList<>();

            potions.forEach(p -> potionItems.add(PotionContents.createItemStack(potionBasedItem, p)));

            if (potionItems.isEmpty()) return;

            if(entryTypeCondensing == EntryTypeCondensing.SEPARATE_COLLECTIONS) {
                String translationKey = potionItems.getFirst().getDescriptionId();

                CondensedEntryRegistry.fromItemStacks(CondensedCreative.location(translationKey), potionItems.getFirst(), potionItems)
                        .setTitleSupplier(() -> {
                            String[] words = Component.translatable(translationKey).getString()
                                    .split(" ");

                            words[wordIndex] = pluralizedWord;

                            return Component.literal(StringUtils.join(words, " "))
                                    .withStyle(ChatFormatting.WHITE);
                        })
                        .addToItemGroup(targetGroup);
            } else {
                allPotionItems.addAll(potionItems);
            }
        }

        if(!allPotionItems.isEmpty()){
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(potionBasedItem);

            String[] words = itemId.getPath().split("_");

            words[words.length - 1] = pluralizedWord.toLowerCase();

            String path = StringUtils.join(words, "_");

            CondensedEntryRegistry.fromItemStacks(CondensedCreative.location(path), potionBasedItem, allPotionItems)
                    .addToItemGroup(targetGroup);
        }
    }

    private static final Predicate<Item> vanillaItemsOnly = item -> Objects.equals(BuiltInRegistries.ITEM.getKey(item).getNamespace(), "minecraft");

    private static Predicate<Item> predicateWithVanillaCheck(Predicate<Item> mainPredicate){
        return (item) -> BuiltinEntries.vanillaItemsOnly.and(mainPredicate).test(item);
    }

    private static Predicate<Item> isSpawnEggItem(Predicate<SpawnEggItem> predicate){
        return (item) -> item instanceof SpawnEggItem spawnEggItem && predicate.test(spawnEggItem);
    }

    @SafeVarargs
    private static <T extends ItemLike> boolean fromTags(Item item, TagKey<T> ...tagKeys){
        if(tagKeys.length == 0) return false;

        boolean isInTag = false;

        for(TagKey<T> tagKey: tagKeys) {
            if (tagKey.isFor(Registries.ITEM)) {
                isInTag = item.builtInRegistryHolder().is((TagKey<Item>) tagKey);
            } else if (tagKey.isFor(Registries.BLOCK)) {
                isInTag = item instanceof BlockItem blockItem && blockItem.getBlock().builtInRegistryHolder().is((TagKey<Block>) tagKey);
            } else {
                LOGGER.warn("It seems that a Condensed Entry was somehow registered with Tag that isn't part of the Item or Block Registry");
            }

            if(isInTag) return true;
        }

        return isInTag;
    }
}
