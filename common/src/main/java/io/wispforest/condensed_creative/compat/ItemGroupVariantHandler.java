package io.wispforest.condensed_creative.compat;

import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

/**
 * A Handler for ItemGroups that may have various tabs within the given ItemGroup
 */
public abstract class ItemGroupVariantHandler<I extends CreativeModeTab> {

    private static final Map<ResourceLocation, ItemGroupVariantHandler<?>> VARIANT_HANDLERS = new HashMap<>();

    private final Class<I> clazz;
    private final ResourceLocation id;

    protected ItemGroupVariantHandler(Class<I> clazz, ResourceLocation id){
        this.clazz = clazz;
        this.id = id;
    }

    //--

    /**
     * Attempts to register a given {@link ItemGroupVariantHandler} to the main registry
     * with check to see if an existing handler for the same ItemGroup clazz exists
     */
    public static <I extends CreativeModeTab> void registerOptional(ItemGroupVariantHandler<I> handler){
        for (ItemGroupVariantHandler<?> value : VARIANT_HANDLERS.values()) {
            if(value.clazz.equals(handler.clazz)) return;
        }

        register(handler);
    }

    /**
     * Register a given {@link ItemGroupVariantHandler} to the main registry
     */
    public static <I extends CreativeModeTab> void register(ItemGroupVariantHandler<I> handler){
        var id = handler.getIdentifier();

        if(VARIANT_HANDLERS.containsKey(id)){
            throw new IllegalStateException("[CondensedCreative]: Unable to register a ItemGroupVariantHandler due to a duplicate entry already existing within register! [ID: " + id + "]");
        }

        VARIANT_HANDLERS.put(id, handler);
    }

    /**
     * Attempt to return an any handler for the given ItemGroup or null if not none are found
     */
    @Nullable
    public static ItemGroupVariantHandler<?> getHandler(CreativeModeTab itemGroup){
        for (var handler : VARIANT_HANDLERS.values()) {
            if(handler.isVariant(itemGroup)) return handler;
        }

        return null;
    }

    /**
     * @return All registered Handlers
     */
    public static Collection<ItemGroupVariantHandler<?>> getHandlers(){
        return VARIANT_HANDLERS.values();
    }

    //--

    /**
     * @return whether the {@link CreativeModeTab} is of the targeted variant
     */
    public final boolean isVariant(CreativeModeTab group){
        return clazz.isInstance(group);
    }

    /**
     * Unique Identifier representing the Variant handler
     */
    public final ResourceLocation getIdentifier(){
        return this.id;
    }

    //--

    /**
     * @return Collection of all selected tabs of the given {@link CreativeModeTab} Variant
     */
    public abstract Collection<Integer> getSelectedTabs(CreativeModeTab group);

    /**
     * @return Get max tabs supported by this {@link CreativeModeTab} Variant
     */
    public abstract int getMaxTabs(CreativeModeTab group);

    //--

}
