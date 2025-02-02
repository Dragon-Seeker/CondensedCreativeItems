package io.wispforest.condensed_creative.fabric;

import io.wispforest.condensed_creative.CondensedCreative;
import io.wispforest.condensed_creative.data.CondensedEntriesLoader;
import io.wispforest.condensed_creative.fabric.compat.owo.OwoCompat;
import io.wispforest.condensed_creative.registry.CondensedEntryRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.CommonLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.item.CreativeModeTab;
import java.util.function.Supplier;

public class CondensedCreativeFabric implements ClientModInitializer {

    public static CreativeModeTab testGroup = null;
    public static Supplier<CreativeModeTab> createOwoItemGroup = () -> null;

    @Override
    public void onInitializeClient() {
        CondensedCreative.onInitializeClient(FabricLoader.getInstance().isDevelopmentEnvironment());

        if (FabricLoader.getInstance().isModLoaded("owo")) OwoCompat.init();

        testGroup = createOwoItemGroup.get();

        //--

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new IdentifierCondensedEntriesLoader());

        if(FabricLoader.getInstance().isDevelopmentEnvironment()) DebugPackLoading.init();

        ClientPlayConnectionEvents.JOIN.register((clientPacketListener, packetSender, minecraft) -> {
            CondensedEntryRegistry.refreshEntrypoints(minecraft.level);
        });

        CommonLifecycleEvents.TAGS_LOADED.register((registries, client) -> {
            if(client) CondensedEntryRegistry.refreshChildren();
        });
        //--
    }

    public static class IdentifierCondensedEntriesLoader extends CondensedEntriesLoader implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return CondensedCreative.location("reload_condensed_entries");
        }
    }
}
