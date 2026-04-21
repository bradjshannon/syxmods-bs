package init.sprite.UI;

import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TILE_SHEET;

public final class UICons {

	private final TILE_SHEET sprite;
	private final SPRITE[] sprites = new SPRITE[16];
	private final UICons tiny;
	
	public UICons(TILE_SHEET sprite) {
		if (sprite.tiles() < 16)
			throw new RuntimeException("array must be longer than 15! " + sprite.tiles());
		this.sprite = sprite;
		for (int i = 0; i < 16; i++) {
			sprites[i] = new SPRITE.SpriteFromSheet(sprite, i);
		}
		tiny = this;
	}
	
	UICons(TILE_SHEET sprite, UICons tiny) {
		if (sprite.tiles() < 16)
			throw new RuntimeException("array must be longer than 15! " + sprite.tiles());
		this.sprite = sprite;
		for (int i = 0; i < 16; i++) {
			sprites[i] = new SPRITE.SpriteFromSheet(sprite, i);
		}
		this.tiny = tiny;
	}
	
	public SPRITE get(int i) {
		return sprites[i];
	}
	
	public SPRITE get(DIR d1, DIR d2, DIR d3, DIR d4){
		int m = 0;
		if (d1 != null)
			m |= d1.mask();
		if (d2 != null)
			m |= d2.mask();
		if (d3 != null)
			m |= d3.mask();
		if (d4 != null)
			m |= d4.mask();
		return get(m);
	}
	
	public SPRITE get(DIR d1, DIR d2, DIR d3){
		return get(d1, d2, d3, null);
	}
	
	public SPRITE get(DIR d1, DIR d2){
		return get(d1, d2, null, null);
	}
	
	public SPRITE get(DIR d1){
		return get(d1, null, null, null);
	}
	
	public static int getIndex(boolean N, boolean E, boolean S, boolean W){
		int nr = 0;
		
		if (N){
			nr |= 0b0001;
		}
		
		if (E){
			nr |= 0b0010;
		}
		
		if (S){
			nr |= 0b0100;
		}
		
		if (W){
			nr |= 0b1000;
		}
		
		return nr;
	}
	
	public void renderBox(SPRITE_RENDERER r, int x1, int y1, int width, int height){
		int M = sprite.size()/4;
		int size = sprite.size();
		if (width <= size-2*M && height <= size-2*M) {
			renderCentered(r, 0, x1+width/2, y1+height/2);
			return;
		}
		
		
		int X1 = x1;
		int Y1 = y1;
		int X2 = x1+width;
		int Y2 = y1+height;
		
		int w = (X2-X1)/size -1;
		int h = (Y2-Y1)/size - 1;

		render(r, DIR.S.mask() | DIR.E.mask(), X1 - M, Y1 - M);

		render(r, DIR.S.mask() | DIR.W.mask(), X2 + M - size, Y1 - M);
		render(r, DIR.N.mask() | DIR.E.mask(), X1 - M, Y2 + M - size);
		render(r, DIR.N.mask() | DIR.W.mask(), X2 + M - size, Y2 + M - size);
		for (int i = 0; i < w; i++) {
			render(r, DIR.S.mask() | DIR.E.mask() | DIR.W.mask(), X1 - M+size+i*size, Y1 - M);
			render(r, DIR.N.mask() | DIR.E.mask() | DIR.W.mask(), X1 - M+size+i*size, Y2 + M- size);
		}
		for (int i = 0; i < h; i++) {
			render(r, DIR.E.mask() | DIR.N.mask() | DIR.S.mask(), X1 - M, Y1 - M+size+i*size);
			render(r, DIR.W.mask() | DIR.N.mask() | DIR.S.mask(), X2 + M- size, Y1 - M+size+i*size);
		}
	}
	
	public void render(boolean N, boolean E, boolean S, boolean W, SPRITE_RENDERER r, int x, int y){
		render(r, getIndex(N, E, S, W), x, y);
	}

	public void render(SPRITE_RENDERER r, int s, int x, int y) {
		if (CORE.renderer().getZoomout() >= 3)
			tiny.sprite.render(r, s, x, y);
		else
			sprite.render(r, s, x, y);
	}
	
	public void render(SPRITE_RENDERER r, int s, int x1, int x2, int y1, int y2) {
		if (CORE.renderer().getZoomout() >= 3)
			tiny.sprite.render(r, s, x1, x2, y1, y2);
		else
			sprite.render(r, s, x1, x2, y1, y2);
	}
	
	public void renderCentered(SPRITE_RENDERER r, int s, int x, int y) {
		if (CORE.renderer().getZoomout() >= 3)
			tiny.sprite.render(r, s, x-tiny.sprite.size()/2, y-tiny.sprite.size()/2);
		else
			sprite.render(r, s, x-sprite.size()/2, y-sprite.size()/2);
	}
	
	public void render(SPRITE_RENDERER r, int s, int corner, int x, int y) {
		render(r, s, x, y);
		if (corner == 0)
			return;
		if (CORE.renderer().getZoomout() >= 3)
			tiny.sprite.render(r, 16+corner, x, y);
		else
			sprite.render(r, 16+corner, x, y);
	}

	
	public int dim() {
		return sprite.size();
	}
	
}
