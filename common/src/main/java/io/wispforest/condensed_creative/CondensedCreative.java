package io.wispforest.condensed_creative;

import io.wispforest.condensed_creative.compat.CondensedCreativeConfig;
import io.wispforest.condensed_creative.registry.CondensedCreativeInitializer;
import io.wispforest.condensed_creative.registry.CondensedEntryRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;

public class CondensedCreative {

    public static final String MODID = "condensed_creative";

    private static ConfigHolder<CondensedCreativeConfig> MAIN_CONFIG;

    private static final boolean DEBUG = Boolean.getBoolean("cci.debug");
    private static boolean DEBUG_ENV = false;

    //---------------------------------------------------------------------------------------------------------

    public static void onInitializeClient(boolean debugEnv) {
        DEBUG_ENV = debugEnv;

        MAIN_CONFIG = AutoConfig.register(CondensedCreativeConfig.class, GsonConfigSerializer::new);

        MAIN_CONFIG.registerSaveListener((configHolder, condensedCreativeConfig) -> {
            CondensedEntryRegistry.refreshEntrypoints();

            return InteractionResult.SUCCESS;
        });

        for(CondensedCreativeInitializer initializer : LoaderSpecificUtils.getEntryPoints()){
            initializer.registerItemGroupVariantHandlers();
        }

        for(CondensedCreativeInitializer initializer : LoaderSpecificUtils.getEntryPoints()){
            initializer.registerCondensedEntries(false);
        }
    }

    public static CondensedCreativeConfig getConfig(){
        return MAIN_CONFIG.getConfig();
    }

    public static boolean isDeveloperMode(){
        return DEBUG_ENV || DEBUG;
    }

    public static ResourceLocation createID(String path){
        return new ResourceLocation(MODID, path);
    }

    //---------------------------------------------------------------------------------------------------------

}
