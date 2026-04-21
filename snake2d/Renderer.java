package snake2d;

import snake2d.util.color.COLOR;
import snake2d.util.color.ColorImp;
import snake2d.util.color.OPACITY;
import snake2d.util.color.OpacityImp;
import snake2d.util.light.AmbientLight;
import snake2d.util.light.LIGHT_AMBIENT;
import snake2d.util.light.LIGHT_POINT;
import snake2d.util.misc.CLAMP;
import snake2d.util.sprite.TextureCoords;

public abstract class Renderer extends CORE_RESOURCE implements SPRITE_RENDERER{
	
	protected int zoomout = 0;
	protected final int pointSize;
	
	private final byte noShadows = (byte) 255;
	private final byte shadows = 0;
	private byte shadowDepth = noShadows;
	private byte lightDepth = shadows;
	
	private int layer = 0;

	Renderer (int pointSize){
		this.pointSize = pointSize;
		zoomout = 0;
	}
	
	final void flush() {
		pflush(pointSize >> zoomout);
		clear();
	}
	
	protected abstract void pflush(int pz);
	
	private int spritesRendered = 0;
	private int shadowsRendered = 0;
	private int particlesRendererd  = 0;
	private int lightsRendererd  = 0;
	private int shadowsRenderedO = 0;
	private int spritesRenderedO = 0;
	private int particlesRendererdO  = 0;
	private int lightsRendererdO  = 0;
	
	private final COLOR white = ColorImp.WHITE100;
	private COLOR current = white;
	private final OPACITY OpacityDefault = OpacityImp.O100;
	private OPACITY currentOpacity = OpacityDefault;
	
	public final int pointsize() {
		return pointSize >> zoomout;
	}
	
	public final int getSpritesSprocessed(){
		return spritesRenderedO;
	}
	
	public final int getParticlesProcessed(){
		return particlesRendererdO;
	}
	
	public final int getLightsProcessed(){
		return lightsRendererdO;
	}
	
	public final int getShadowsRendered() {
		return shadowsRenderedO;
	}
	
    public final void setColor(COLOR color){
    	current = color;
    }
    
    public final COLOR colorGet() {
    	return current;
    }
    
    public final COLOR getBoundColor() {
    	return current;
    }
    
    public final void setNormalColor(){
    	current = white;
    }
    
	public final void setOpacity(OPACITY o){
		currentOpacity = o;
	}
	
	public final void setNormalOpacity(){
		currentOpacity = OpacityDefault;
	}
	
	public final boolean isNormalOpacity(){
		return currentOpacity == OpacityDefault;
	}
    
	/**
	 * 
	 * @param shade shadows shade all light vs no lights
	 */
	public final void shadeLight(boolean shade){
		lightDepth = shade ? shadows : noShadows;
	}
	
	/**
	 * set depth. Lights with smaller or equal depth will be shaded by the shadows
	 * @param depth
	 */
	public final void shadowDepthSet(byte depth) {
		this.shadowDepth = depth;
	}
	
	/**
	 * Set shadow depth to shade all lights
	 */
	public final void shadowDepthDefault() {
		this.shadowDepth = noShadows;
	}
	
	/**
	 * set a depth. Shadows with higher or equal dephs will shade lights
	 * @param depth
	 */
	public final void lightDepthSet(byte depth) {
		lightDepth = depth;
	}
	
	@Override
	public final void renderSprite(int x1, int x2, int y1, int y2, TextureCoords t){
		renderTextured(x1, x2, y1, y2, t, t);
	}
	
	public final void renderTextured(int x1, int x2, int y1, int y2, 
			TextureCoords texture, 
			TextureCoords stencil){
		
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			x2 = x2 >> zoomout;
			y1 = y1 >> zoomout;
			y2 = y2 >> zoomout;
		}
		if (x2 < 0 || y2 < 0 || x1 > CORE.getGraphics().nativeWidth || y1 > CORE.getGraphics().nativeHeight)
			return;
		renderSprite(
				stencil, 
				texture, 
				x1, x2, y1, y2, current, currentOpacity);
		
		spritesRendered ++;
	}
	
	protected abstract void renderSprite(
			TextureCoords t, 
			TextureCoords to, 
    		int x1, int x2, int y1, int y2, 
    		COLOR color, OPACITY opacity);
	
	public final void renderDisplaced(int x1, int x2, int y1, int y2, 
			TextureCoords displacement, 
			TextureCoords texture){
		
		renderDisplace(displacement.x1, displacement.y1, texture.x1, texture.y1, texture.x2-texture.x1, texture.y2-texture.y1, 16, x1, x2, y1, y2);
		
	}
	
	public final void renderDisplaced(int x1, int x2, int y1, int y2, double scale,
			TextureCoords displacement, 
			TextureCoords texture){
		
		renderDisplace(displacement.x1, displacement.y1, texture.x1, texture.y1, texture.x2-texture.x1, texture.y2-texture.y1, scale*16.0, x1, x2, y1, y2);
		
	}
	
	public final void renderDisplace(float tx1, float ty1, float dx1, float dy1, int w, int h, double scale, int x1, int x2, int y1, int y2) {
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			x2 = x2 >> zoomout;
			y1 = y1 >> zoomout;
			y2 = y2 >> zoomout;
		}
		
		renderDisplace(tx1, ty1, dx1, dy1, w, h, scale, x1, x2, y1, y2, current, currentOpacity);
		spritesRendered ++;
	}

	protected abstract void renderDisplace(float tx1, float ty1, float dx1, float dy1, int w, int h, double scale, int x1, int x2, int y1, int y2, COLOR color, OPACITY opacity);
	
	
	private final void renderShadow(
    		TextureCoords t, 
    		int x1, int y1, int x2, int y2,
    		int x3, int y3, int x4, int y4, byte d){
		
    	renderShadow(t, 
    			x1, y1, x2, y2, x3, y3, x4, y4, d, shadowDepth);
    	shadowsRendered ++;
	}
	
	protected abstract void renderShadow(
			TextureCoords t, 
    		int x1, int y1, int x2, int y2,
    		int x3, int y3, int x4, int y4, byte d, byte depth
    		);
	
	public final void renderShadow(int x1, int x2, int y1, int y2, TextureCoords stencil, byte d){
		
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			x2 = x2 >> zoomout;
			y1 = y1 >> zoomout;
			y2 = y2 >> zoomout;
		}
		
		if (x2 < 0 || y2 < 0 || x1 > CORE.getGraphics().nativeWidth || y1 > CORE.getGraphics().nativeHeight)
			return;
		
		renderShadow(
				stencil, 
				x1, y1, 
				x2, y1, 
				x2, y2, 
				x1, y2, d
				);
	}
	
	public final void setMaxDepth(int x1, int x2, int y1, int y2, TextureCoords stencil){
		
		setMaxDepth(x1, x2, y1, y2, stencil, layer);

	}
	
	public final void setMaxDepth(int x1, int x2, int y1, int y2, TextureCoords stencil, int layer){
		
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			x2 = x2 >> zoomout;
			y1 = y1 >> zoomout;
			y2 = y2 >> zoomout;
		}
		
		if (x2 < 0 || y2 < 0 || x1 > CORE.getGraphics().nativeWidth || y1 > CORE.getGraphics().nativeHeight)
			return;
		
		psetMaxDepth(x1, x2, y1, y2, stencil, layer);

	}
	
	protected abstract void psetMaxDepth(int x1, int x2, int y1, int y2, TextureCoords stencil, int depth);
	
	public int getDepth() {
		return layer;
	}
	
	public final void renderParticle(int x, int y){
		renderParticle(x, y, (byte) 128, (byte) 128, (byte) 255, (byte)255);
	}
    
	public final void renderParticleFlat(int x, int y){
		renderParticle(x, y, (byte) 128, (byte) 128, (byte) 255, (byte)0);
	}
	
	public final void renderParticle(int x, int y, byte nX, byte nY, byte nZ, byte nA){
		if (zoomout > 2) {
			return;
		}
		if (zoomout != 0){
			x = x >> zoomout;
			y = y >> zoomout;
		}
		
		if (x < 0 || y < 0 || x > CORE.getGraphics().nativeWidth || y > CORE.getGraphics().nativeHeight)
			return;
		
		renderParticle((short)x, (short)y, nX, nY, nZ, nA, current, currentOpacity);
		particlesRendererd ++;
	}
	
	protected abstract void renderParticle(short x, short y, byte nx, byte ny, byte nz, byte nA, COLOR color, OPACITY opacity);
	
	
	public final void registerLight(LIGHT_POINT light, int x1, int x2, int y1, int y2){
		registerLight(light, x1, x2, y1, y2, (byte)255, (byte)255, (byte)255, (byte)255);
	}
	
	/**
	 * 
	 * @param light
	 * @param x1
	 * @param x2
	 * @param y1
	 * @param y2
	 * @param mask 0b0000|NW|SW|SE|NE
	 */
	public final void registerLight(LIGHT_POINT light, int x1, int x2, int y1, int y2, byte ne, byte se, byte sw, byte nw){
		lightsRendererd ++;
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			x2 = x2 >> zoomout;
			y1 = y1 >> zoomout;
			y2 = y2 >> zoomout;
			registerLight(light, (int)light.cx()>>zoomout, (int)light.cy()>>zoomout, (int)light.cz(), light.getRadius() >> zoomout, x1, x2, y1, y2, ne, se, sw, nw, lightDepth);
			return;
		}
		registerLight(light, light.cx(), light.cy(), light.cz(), light.getRadius(), x1, x2, y1, y2, ne, se, sw, nw, lightDepth);
	}
	
	protected abstract void registerLight(LIGHT_POINT l, float x, float y, float z, int radius, 
    		int x1, int x2, int y1, int y2, byte ne, byte se, byte sw, byte nw, byte depth);
	
	public final void registerAmbient(LIGHT_AMBIENT light, int x1, int x2, int y1, int y2){
		lightsRendererd ++;
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			x2 = x2 >> zoomout;
			y1 = y1 >> zoomout;
			y2 = y2 >> zoomout;
		}
		registerAmbient(light, x1, x2, y1, y2, lightDepth);
	}
	
	protected abstract void registerAmbient(LIGHT_AMBIENT l, 
    		int x1, int x2, int y1, int y2, byte depth);
	
	/**
	 * 
	 * @param keeplight - the lights from previous layer?
	 * @param zoomout - how much zoom-out, will be an exponent of two.
	 * @return the index of this layer
	 */
	public final int newLayer(boolean keeplight, int zoomout){
		this.zoomout = zoomout;
		int pz = CLAMP.i(pointSize >> zoomout, 1, pointSize);
		layer = pnewLayer(keeplight, pz);
		return layer;
	}
	
	
	/**
	 * Makes a new layer. This layer and all the following will be lit with the same light. Should only
	 * be called once. For optimisation (need only upload the added lights light and process them 
	 * once for all layers.)
	 * @param zoomout
	 */
	
	private Object debugOld = null;
	public final int newFinalLightWithShadows(int zoomout, Object o){
		this.zoomout = zoomout;
		int pz = CLAMP.i(pointSize >> zoomout, 1, pointSize);
		if (debugOld != null && debugOld != o)
			throw new RuntimeException(debugOld + " " + o);
		debugOld = o;
		layer = pnewFinalLightLayer(pz);
		return layer;
	}
	
	protected abstract int pnewLayer(boolean keepLights, int pointSize);
	protected abstract int pnewFinalLightLayer(int pointSize);
	
	public void clear(){
		pclear(pointSize >> zoomout);
		spritesRenderedO = spritesRendered;
		particlesRendererdO = particlesRendererd;
		lightsRendererdO = lightsRendererd;
		shadowsRenderedO = shadowsRendered;
		shadowsRendered = 0;
		spritesRendered = 0;
		particlesRendererd = 0;
		lightsRendererd = 0;
		zoomout = 0;
		layer = 0;
		debugOld = null;
	}

	protected abstract void pclear(int pointSize);
	
	public int getZoomout() {
		return zoomout;
	}
	
	public void renderTileLight(int x1, int y1, int dim, byte nw, byte ne, byte se, byte sw) {
		if (zoomout != 0){
			x1 = x1 >> zoomout;
			y1 = y1 >> zoomout;
			dim = dim >> zoomout;
		}
		renderTilelight(x1, y1, dim, nw, ne, se, sw);
	}
	
	public void setTileLight(AmbientLight l) {
		setTileLight(l, shadowDepth);
	}
	
	
	protected abstract void setTileLight(LIGHT_AMBIENT l, byte depth);
	
	protected abstract void renderPointlight(int x, int y, int z, int radius);
	
	protected abstract void setPointLight(LIGHT_POINT l, byte depth);

	protected abstract void renderTilelight(int x1, int y1, int dim, byte nw, byte ne, byte se, byte sw);
	
	public void renderUniLight(int x, int y, int z, int radius) {
		if (zoomout != 0){
			x = x >> zoomout;
			y = y >> zoomout;
			z = z >> zoomout;
			radius = radius >> zoomout;
		}
		renderPointlight(x, y, z, radius);
	}
	
	public void setUniLight(LIGHT_POINT l) {
		setPointLight(l, shadowDepth);
	}

	public void setZoom(int i) {
		zoomout = i;
	}
	
}
