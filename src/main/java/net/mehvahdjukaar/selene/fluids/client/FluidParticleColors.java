package net.mehvahdjukaar.selene.fluids.client;

import net.mehvahdjukaar.selene.Selene;
import net.mehvahdjukaar.selene.fluids.SoftFluid;
import net.mehvahdjukaar.selene.fluids.SoftFluidRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;

public class FluidParticleColors {
    public static HashMap<String, Integer> particleColor = new HashMap<>();

    //TODO: possibly do it for ALL fluids, not only non grayscale ones
    public static void refresh() {
        particleColor = new HashMap<>();
        for (Fluid f : ForgeRegistries.FLUIDS) {
            String key = f.getRegistryName().toString();
            if (!particleColor.containsKey(key)) {
                ResourceLocation location = f.getAttributes().getStillTexture();
                if (location == null) continue;
                TextureAtlas textureMap = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
                TextureAtlasSprite sprite = textureMap.getSprite(location);
                int averageColor = -1;
                try {
                    averageColor = getColorFrom(sprite, f.getAttributes().getColor());
                } catch (Exception e) {
                    Selene.LOGGER.warn("Failed to load particle color for " + sprite.toString() + " using current resource pack. might be a broken png.mcmeta");
                }
                particleColor.put(key, averageColor);
            }
        }
        for (SoftFluid s : SoftFluidRegistry.getFluids()) {
            if (!particleColor.containsKey(s.getID()) && !s.isColored()) {
                ResourceLocation location = s.getStillTexture();
                if (location == null) continue;
                TextureAtlas textureMap = Minecraft.getInstance().getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
                TextureAtlasSprite sprite = textureMap.getSprite(location);
                int averageColor = -1;
                try {
                    averageColor = getColorFrom(sprite, s.getTintColor());
                } catch (Exception e) {
                    Selene.LOGGER.warn("Failed to load particle color for " + sprite + " using current resource pack. might be a broken png.mcmeta");
                }
                particleColor.put(s.getID(), averageColor);
            }
        }
    }

    public static int get(Fluid f) {
        return particleColor.getOrDefault(f.getRegistryName().toString(), -1);
    }

    public static int get(String s) {
        return particleColor.getOrDefault(s, -1);
    }


    //credits to Random832
    private static int getColorFrom(TextureAtlasSprite sprite, int tint) {
        if (sprite == null || sprite.getFrameCount() == 0) return -1;

        int tintR = tint >> 16 & 255;
        int tintG = tint >> 8 & 255;
        int tintB = tint & 255;
        int total = 0, totalR = 0, totalB = 0, totalG = 0;

        for (int tryFrame = 0; tryFrame < sprite.getFrameCount(); tryFrame++) {
            try {
                for (int x = 0; x < sprite.getWidth(); x++) {
                    for (int y = 0; y < sprite.getHeight(); y++) {

                        int pixel = sprite.getPixelRGBA(tryFrame, x, y);

                        // this is in 0xAABBGGRR format, not the usual 0xAARRGGBB.
                        int pixelB = pixel >> 16 & 255;
                        int pixelG = pixel >> 8 & 255;
                        int pixelR = pixel & 255;
                        ++total;
                        totalR += pixelR;
                        totalG += pixelG;
                        totalB += pixelB;
                    }
                }
                break;
            } catch (Exception e) {
                total = 0;
                totalR = 0;
                totalB = 0;
                totalG = 0;
            }
        }
        if (total <= 0) return -1;
        return FastColor.ARGB32.color(255,
                totalR / total * tintR / 255,
                totalG / total * tintG / 255,
                totalB / total * tintB / 255);
    }


}
