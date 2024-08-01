package io.wispforest.condensed_creative.registry;

import io.wispforest.condensed_creative.CondensedCreative;
import net.minecraft.core.RegistryAccess;

/**
 *  Entry point Interface where you can create and register condensed Entries.
 *
 *  <br><br>
 *
 *  Example Entry Point within fabric.mod.json:
 *
 *  <pre>
 *  ...
 *  "condensed_creative": [
 *    "the.class.path.here"
 *  ]
 *  ...
 *  <pre/>
 */
public interface CondensedCreativeInitializer {

    /**
     * @deprecated Use {@link #registerCondensedEntries(boolean, RegistryAccess)} instead
     */
    @Deprecated
    default void registerCondensedEntries(boolean refreshed){}

    /**
     * This happens during the Clientside Loading for {@link CondensedCreative#onInitializeClient} and
     * recommend implementing this in a separate class not touched by any of your code to prevent class loading problems
     */
    default void registerCondensedEntries(boolean refreshed, RegistryAccess access){
        registerCondensedEntries(refreshed);
    }

    /**
     * Proper place to register any Handlers for ItemGroup variants that have tabs within scuh
     */
    default void registerItemGroupVariantHandlers(){}

    /**
     * Used for the Forge Loader ONLY!
     */
    @interface InitializeCondensedEntries{}
}
