package mods.battlegear2.client.renderer;

import mods.battlegear2.api.heraldry.IFlagHolder;
import mods.battlegear2.client.utils.ImageCache;
import net.minecraft.block.BlockAir;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.util.List;

/**
 * User: nerd-boy
 * Date: 2/08/13
 * Time: 2:33 PM
 * TODO: Add discription
 */
public class FlagPoleTileRenderer extends TileEntitySpecialRenderer {
    public static int period = 250;
    public static int flag_sections = 16;
    public static double getZLevel(float x, float size, long time){
        return Math.pow(x, 0.5/(size/5)) * Math.sin(Math.PI * ( -x/size * 3 + ((float)(time% period)) / (0.5F*(float)period))) / 4;
    }

    @Override
    public void renderTileEntityAt(TileEntity tileentity, double d0, double d1, double d2, float f, int damage) {

        if(tileentity instanceof IFlagHolder){
            IBlockState banner = tileentity.getWorld().getBlockState(tileentity.getPos());
            if (banner.getBlock() instanceof BlockAir) {
                return;
            }
            Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.locationBlocksTexture);
            int type = banner.getBlock().getMetaFromState(banner);
            int side = ((IFlagHolder) tileentity).getOrientation();

            GlStateManager.pushMatrix();
            GlStateManager.translate(d0, d1, d2);
            GlStateManager.color(1, 1, 1);

            float[] dims = new float[5];
            for(int i=0; i<5; i++){
                dims[i] = ((IFlagHolder)tileentity).getTextureDimensions(type, i);
            }
            switch (side){
                case 0:
                    renderYFlag((IFlagHolder)tileentity, d0, d1, d2, f, type);
                    break;
                case 1:
                    renderZFlag((IFlagHolder)tileentity, d0, d1, d2, f, type);
                    break;
                case 2:
                    GlStateManager.rotate(90, 0, 1, 0);
                    GlStateManager.translate(-1, 0, 0);
                    renderZFlag((IFlagHolder)tileentity, d0, d1, d2, f, type);
                    break;
            }

            GlStateManager.popMatrix();
        }
    }

    private void renderZFlag(IFlagHolder tileentity, double d0, double d1, double d2, float f, int type) {

        List<ItemStack> flags = tileentity.getFlags();
        if(flags.size()>0)
        {
            Tessellator tess = Tessellator.getInstance();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GlStateManager.disableLighting();
            GlStateManager.matrixMode(GL11.GL_TEXTURE);
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-90, 0, 0, 1);
            for(int flagIndex = 0; flagIndex < flags.size(); flagIndex++){
                ItemStack flag = flags.get(flagIndex);
                ImageCache.setTexture(flag);

                if(flag_sections == 0){
                    tess.getWorldRenderer().startDrawingQuads();

                    tess.getWorldRenderer().addVertexWithUV(8F / 16F, -flagIndex + 1 - 2F / 16F, 0, 0, 0.001);
                    tess.getWorldRenderer().addVertexWithUV(8F / 16F, -flagIndex - 2F / 16F, 0, 1.00, 0.001);
                    tess.getWorldRenderer().addVertexWithUV(8F / 16F, -flagIndex - 2F / 16F, 1, 1.00, 0.999);
                    tess.getWorldRenderer().addVertexWithUV(8F / 16F, -flagIndex + 1 - 2F / 16F, 1, 0, 0.999);

                    tess.getWorldRenderer().addVertexWithUV(8F / 16F, -flagIndex + 1 - 2F / 16F, 1, 0, 0.999);
                    tess.getWorldRenderer().addVertexWithUV(8F / 16F, -flagIndex - 2F / 16F, 1, 1.00, 0.999);
                    tess.getWorldRenderer().addVertexWithUV(8F / 16F, -flagIndex - 2F / 16F, 0, 1.00, 0.001);
                    tess.getWorldRenderer().addVertexWithUV(8F / 16F, -flagIndex + 1 - 2F / 16F, 0, 0, 0.001);


                    tess.draw();

                }else{

                    long time = System.currentTimeMillis();
                    for(int i = 0; i < flag_sections; i++){
                        tess.getWorldRenderer().startDrawingQuads();

                        double z1 = getZLevel((float)((flag_sections - i)) / (float)flag_sections + flagIndex, 5, time) / 5F;
                        double z2 = getZLevel((float)(flag_sections - i+1) / (float)flag_sections + flagIndex, 5, time) / 5F;

                        tess.getWorldRenderer().addVertexWithUV(8F / 16F + z1, -flagIndex + (float) (i + 1) / (float) flag_sections - 2F / 16F, 0, (float) (i + 1) / (float) flag_sections, 0.999);
                        tess.getWorldRenderer().addVertexWithUV(8F / 16F + z2, -flagIndex + (float) (i) / (float) flag_sections - 2F / 16F, 0, (float) (i) / (float) flag_sections, 0.999);
                        tess.getWorldRenderer().addVertexWithUV(8F / 16F + z2, -flagIndex + (float) (i) / (float) flag_sections - 2F / 16F, 1, (float) (i) / (float) flag_sections, 0.001);
                        tess.getWorldRenderer().addVertexWithUV(8F / 16F + z1, -flagIndex + (float) (i + 1) / (float) flag_sections - 2F / 16F, 1, (float) (i + 1) / (float) flag_sections, 0.001);

                        tess.getWorldRenderer().addVertexWithUV(8F / 16F + z1, -flagIndex + (float) (i + 1) / (float) flag_sections - 2F / 16F, 1, (float) (i + 1) / (float) flag_sections, 0.001);
                        tess.getWorldRenderer().addVertexWithUV(8F / 16F + z2, -flagIndex + (float) (i) / (float) flag_sections - 2F / 16F, 1, (float) (i) / (float) flag_sections, 0.001);
                        tess.getWorldRenderer().addVertexWithUV(8F / 16F + z2, -flagIndex + (float) (i) / (float) flag_sections - 2F / 16F, 0, (float) (i) / (float) flag_sections, 0.999);
                        tess.getWorldRenderer().addVertexWithUV(8F / 16F + z1, -flagIndex + (float) (i + 1) / (float) flag_sections - 2F / 16F, 0, (float) (i + 1) / (float) flag_sections, 0.999);


                        tess.draw();
                    }

                }

            }

            GlStateManager.popMatrix();
            GlStateManager.matrixMode(GL11.GL_MODELVIEW);
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
        }
    }

    private void renderYFlag(IFlagHolder tileentity, double d0, double d1, double d2, float f, int type) {

        List<ItemStack> flags = tileentity.getFlags();
        if(flags.size()>0)
        {
            Tessellator tess = Tessellator.getInstance();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

            GlStateManager.disableLighting();
            for(int flagIndex = 0; flagIndex < flags.size(); flagIndex++){
                ItemStack flag = flags.get(flagIndex);
                ImageCache.setTexture(flag);


                if(flag_sections == 0){
                    tess.getWorldRenderer().startDrawingQuads();
                    tess.getWorldRenderer().addVertexWithUV(7F / 16F - flagIndex, 0, 8F / 16F, 0, 0.999);
                    tess.getWorldRenderer().addVertexWithUV(7F / 16F - flagIndex - 1, 0, 8F / 16F, 1.0025, 0.999);
                    tess.getWorldRenderer().addVertexWithUV(7F / 16F - flagIndex - 1, 1, 8F / 16F, 1.0025, 0.001);
                    tess.getWorldRenderer().addVertexWithUV(7F / 16F - flagIndex, 1, 8F / 16F, 0, 0.001);

                    tess.getWorldRenderer().addVertexWithUV(7F / 16F - flagIndex, 1, 8F / 16F, 0, 0.001);
                    tess.getWorldRenderer().addVertexWithUV(7F / 16F - flagIndex - 1, 1, 8F / 16F, 1.0025, 0.001);
                    tess.getWorldRenderer().addVertexWithUV(7F / 16F - flagIndex - 1, 0, 8F / 16F, 1.0025, 0.999);
                    tess.getWorldRenderer().addVertexWithUV(7F / 16F - flagIndex, 0, 8F / 16F, 0, 0.999);

                    tess.draw();

                }else{
                    long time = System.currentTimeMillis();
                    for(int i = 0; i < flag_sections; i++){
                        tess.getWorldRenderer().startDrawingQuads();

                        double z1 = getZLevel((float)(i) / (float)flag_sections + flagIndex, 3, time);
                        double z2 = getZLevel((float)(i+1) / (float)flag_sections + flagIndex, 3, time);

                        tess.getWorldRenderer().addVertexWithUV(7F / 16F - (float) (i) / (float) flag_sections - flagIndex, 0, 8F / 16F + z1, (float) (i) / (float) flag_sections, 0.999);
                        tess.getWorldRenderer().addVertexWithUV(7F / 16F - (float) (i + 1) / (float) flag_sections - flagIndex, 0, 8F / 16F + z2, (float) (i + 1) / (float) flag_sections, 0.999);
                        tess.getWorldRenderer().addVertexWithUV(7F / 16F - (float) (i + 1) / (float) flag_sections - flagIndex, 1.0025, 8F / 16F + z2, (float) (i + 1) / (float) flag_sections, 0.001);
                        tess.getWorldRenderer().addVertexWithUV(7F / 16F - (float) (i) / (float) flag_sections - flagIndex, 1.0025, 8F / 16F + z1, (float) (i) / (float) flag_sections, 0.001);

                        tess.getWorldRenderer().addVertexWithUV(7F / 16F - (float) (i) / (float) flag_sections - flagIndex, 1.0025, 8F / 16F + z1, (float) (i) / (float) flag_sections, 0.001);
                        tess.getWorldRenderer().addVertexWithUV(7F / 16F - (float) (i + 1) / (float) flag_sections - flagIndex, 1.0025, 8F / 16F + z2, (float) (i + 1) / (float) flag_sections, 0.001);
                        tess.getWorldRenderer().addVertexWithUV(7F / 16F - (float) (i + 1) / (float) flag_sections - flagIndex, 0, 8F / 16F + z2, (float) (i + 1) / (float) flag_sections, 0.999);
                        tess.getWorldRenderer().addVertexWithUV(7F / 16F - (float) (i) / (float) flag_sections - flagIndex, 0, 8F / 16F + z1, (float) (i) / (float) flag_sections, 0.999);

                        tess.draw();
                    }
                }
            }
            GlStateManager.enableLighting();
            GlStateManager.disableBlend();
        }
    }
}
