package io.wispforest.condensed_creative.fabric;

import io.wispforest.condensed_creative.CondensedCreative;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.ResourcePackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.ModOrigin;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class DebugPackLoading {

    protected static void init(){
        //Work around for differing folder paths with the Debug pack loading in Common
        try {
            ModContainer modContainer = FabricLoader.getInstance().getModContainer("condensed_creative").get();

            Path mainDevFolder = modContainer.getRootPaths().get(0).getParent().getParent().getParent().getParent();

            DebugModContainer container = new DebugModContainer(FabricLoader.getInstance().getModContainer("condensed_creative").get());

            container.addAdditionalRootPath(mainDevFolder.resolve("common\\build\\resources\\main"));

            boolean success = ResourceManagerHelper.registerBuiltinResourcePack(
                    CondensedCreative.location("dev_pack"),
                    container,
                    ResourcePackActivationType.NORMAL
            );

            if (!success) System.out.println("WEE WOO WEE WOO WEE WOO");
        } catch (Exception ignore){
            System.out.println("IGNORE (Condensed Creative Internal Debug Error): It seems that the Debug Pack has not been loaded!");
        }
    }

    private static class DebugModContainer implements ModContainer {

        public List<Path> additionalRootPaths = new ArrayList<>();

        private ModContainer wrappedContainer;

        public DebugModContainer(ModContainer container){
            this.wrappedContainer = container;
        }

        @Override
        public ModMetadata getMetadata() {
            return wrappedContainer.getMetadata();
        }

        @Override
        public List<Path> getRootPaths() {
            List<Path> rootPaths = new ArrayList<>();

            rootPaths.addAll(wrappedContainer.getRootPaths());
            rootPaths.addAll(additionalRootPaths);

            return rootPaths;
        }

        public void addAdditionalRootPath(Path path){
            this.additionalRootPaths.add(path);
        }

        @Override
        public ModOrigin getOrigin() {
            return wrappedContainer.getOrigin();
        }

        @Override
        public Optional<ModContainer> getContainingMod() {
            return wrappedContainer.getContainingMod();
        }

        @Override
        public Collection<ModContainer> getContainedMods() {
            return wrappedContainer.getContainedMods();
        }

        @Override
        public Path getRootPath() {
            return wrappedContainer.getRootPath();
        }

        @Override
        public Path getPath(String file) {
            return wrappedContainer.getPath(file);
        }
    }
}
