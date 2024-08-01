package io.wispforest.condensed_creative.neoforge;


import io.wispforest.condensed_creative.CondensedCreative;
import io.wispforest.condensed_creative.compat.CondensedCreativeConfig;
import io.wispforest.condensed_creative.data.CondensedEntriesLoader;
import io.wispforest.condensed_creative.registry.CondensedEntryRegistry;
import me.shedaniel.autoconfig.AutoConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterClientReloadListenersEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@Mod(value = CondensedCreative.MODID, dist = Dist.CLIENT)
public class CondensedCreativeClientNeoforge {

    private final IEventBus eventBus;

    public CondensedCreativeClientNeoforge(IEventBus eventBus) {
        this.eventBus = eventBus;

        eventBus.addListener(this::setupClient);
        eventBus.addListener(this::registerReloadListener);
        NeoForge.EVENT_BUS.addListener(this::refreshOnLogin);
        NeoForge.EVENT_BUS.addListener(this::refreshChildrenEntries);
    }

    public void setupClient(final FMLClientSetupEvent event){
        event.enqueueWork(() -> {
            CondensedCreative.onInitializeClient(!FMLEnvironment.production);

            ModLoadingContext.get()
                    .registerExtensionPoint(IConfigScreenFactory.class, () -> (client, parent) -> AutoConfig.getConfigScreen(CondensedCreativeConfig.class, parent).get());
        });
    }

    public void refreshOnLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        CondensedEntryRegistry.refreshEntrypoints(event.getPlayer().level());
    }

    public void registerReloadListener(RegisterClientReloadListenersEvent event){
        event.registerReloadListener(new CondensedEntriesLoader());
    }

    public void refreshChildrenEntries(TagsUpdatedEvent event) {
        if(event.getUpdateCause().equals(TagsUpdatedEvent.UpdateCause.CLIENT_PACKET_RECEIVED)) {
            CondensedEntryRegistry.refreshChildren();
        }
    }
}
