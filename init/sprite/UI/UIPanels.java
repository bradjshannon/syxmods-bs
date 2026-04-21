package init.sprite.UI;

import java.io.IOException;

import init.paths.PATHS;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.DIR;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.sprite.TILE_SHEET;
import util.spritecomposer.ComposerDests;
import util.spritecomposer.ComposerSources;
import util.spritecomposer.ComposerThings;
import util.spritecomposer.ComposerThings.ITileSheet;
import util.spritecomposer.ComposerUtil;

public final class UIPanels {


	public final UIPanel thin;
	public final UIPanel butt;
	public final UIPanel big;
	public TILE_SHEET panelClose;
	public final TitleBox[] titleBoxes;


	UIPanels() throws IOException {

		new ComposerThings.IInit(PATHS.SPRITE_UI().get("Panels"), 504, 84) {
			
			@Override
			protected void init(ComposerUtil c, ComposerSources s, ComposerDests d) throws IOException {
				s.full.init(0, 0, 4, 1, 3, 3, d.s24);
				super.init(c, s, d);
			}
			
		};
		
		thin = new UIPanel(0, 5, 3);
		butt = new UIPanel(1, 3, 1);
		big = new UIPanel(2, 12, 9);
		
		titleBoxes = new TitleBox[3];
		
		{
			TILE_SHEET s = 	new ITileSheet(PATHS.SPRITE_UI().get("TitleBox"), 312, 140) {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, 0, 1, 1, 3, 1, d.s24);
					s.full.paste(true);
					return d.s24.saveGui();
				}
			}.get();
			titleBoxes[0] = new TitleBoxN(24, s);
			
			panelClose = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(s.full.body().x2(), 0, 1, 1, 2, 1, d.s24);
					s.full.paste(true);
					return d.s24.saveGui();
				}
			}.get();
			
			s = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, s.full.body().y2(), 1, 1, 3, 1, d.s32);
					s.full.paste(true);
					return d.s32.saveGui();
				}
			}.get();
			titleBoxes[1] = new TitleBoxN(32, s);
			
			final TILE_SHEET ss = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.init(0, s.full.body().y2(), 1, 1, 6, 2, d.s24);
					s.full.paste(true);
					return d.s24.saveGui();
				}
			}.get();
			
			titleBoxes[2] = new TitleBox(48) {

				
				@Override
				public void render(SPRITE_RENDERER r, int x1, int y1, int width) {
					renderP(r, 0, x1-height, y1);
					for (int w = 0; w + height < width; w++) {
						renderP(r, 1, x1+w, y1);
					}
					renderP(r, 1, x1+width-height, y1);
					renderP(r, 2, x1+width, y1);
				}
				
				private void renderP(SPRITE_RENDERER r, int t, int x1, int y1) {
					
					ss.render(r, t*2, x1, y1);
					ss.render(r, t*2+1, x1+24, y1);
					ss.render(r, t*2+6, x1, y1+24);
					ss.render(r, t*2+7, x1+24, y1+24);
				}

	
				
			};
			
		}
		
	}

	public TitleBox titleBox(int height) {
		for (TitleBox b : titleBoxes) {
			if (height <= b.height-8)
				return b;
		}
		return titleBoxes[titleBoxes.length-1];
	}
	
	public static abstract class TitleBox {
		
		public int height;
		
		protected TitleBox(int height) {
			this.height = height;
		}
		
		public abstract void render(SPRITE_RENDERER r, int x1, int y1, int width);
		public void renderCY(SPRITE_RENDERER r, int x1, int cy, int width) {
			render(r, x1, cy-height/2, width);
		}
	}
	
	public static class TitleBoxN extends TitleBox {
		
		private final TILE_SHEET sheet;

		private TitleBoxN(int height, TILE_SHEET sheet) {
			super(height);
			this.sheet = sheet;
		}

		@Override
		public void render(SPRITE_RENDERER r, int x1, int y1, int width) {
			
			sheet.render(r, 0, x1-height, y1);
			for (int w = 0; w + height < width; w++) {
				sheet.render(r, 1, x1+w, y1);
			}
			sheet.render(r, 1, x1+width-height, y1);
			sheet.render(r, 2, x1+width, y1);
		}


	}
	
	public static class UIPanel {
		
		public final int margin;
		private final int min;
		public final int tMid;
		private final TILE_SHEET sheet;
		public static final int dim = 24;
		
		private final static int[] toBox = new int[16];
		static {
			toBox[DIR.N.mask() | DIR.W.mask()] = 0;
			toBox[DIR.N.mask()] = 1;
			toBox[DIR.N.mask() | DIR.E.mask()] = 2;
			toBox[DIR.W.mask()] = 3;
			toBox[0] = 4;
			toBox[DIR.E.mask()] = 5;
			toBox[DIR.W.mask() | DIR.S.mask()] = 6;
			toBox[DIR.S.mask()] = 7;
			toBox[DIR.E.mask() | DIR.S.mask()] = 8;
		}
		
		UIPanel(int variation, int margin, int tMid) throws IOException{
			this.sheet = new ITileSheet() {

				@Override
				protected TILE_SHEET init(ComposerUtil c, ComposerSources s, ComposerDests d) {
					s.full.setVar(variation);
					s.full.paste(true);
					return d.s24.saveGui();
				}
				
			}.get();
			this.tMid = tMid;
			this.margin = margin;
			min = dim-margin;
		}

		public int dim(int dim, int margin) {
			return dim+margin*2+this.margin*2;
		}
		
		public void render(SPRITE_RENDERER r, RECTANGLE body, int margin, int dirmask) {
			this.render(r, body.x1(), body.x2(), body.y1(), body.y2(), margin, dirmask);
			
		}
		
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2, int margin, int dirmask) {
			
			X1 -=margin;
			int width = X2-X1+margin;
			Y1 -=margin;
			int height = Y2-Y1+margin;
			
			if (width < min)
				width = min;
			
			if (height < min)
				height = min;
			
			X2 = X1+width;
			Y2 = Y1+height;
			
			X1 -= this.margin;
			X2 += this.margin;
			Y1 -= this.margin;
			Y2 += this.margin;

			
			for (int x = X1; x < X2; x+= dim) {
				if (x == X1)
					render(r, x, Y1, DIR.W, DIR.N, dirmask);
				else if (x + dim >= X2) {
					render(r, X2-dim, Y1, DIR.E, DIR.N, dirmask);
				}else {
					render(r, x, Y1, DIR.N, DIR.N, dirmask);
				}
			}
			Y1 += dim;
			while(Y1+dim < Y2) {
				for (int x = X1; x < X2; x+= dim) {
					if (x == X1)
						render(r, x, Y1, DIR.W, DIR.W, dirmask);
					else if (x + dim >= X2) {
						render(r, X2-dim, Y1, DIR.E, DIR.E, dirmask);
					}else {
						render(r, x, Y1, DIR.C, DIR.C, dirmask);
					}
				}
				Y1 += dim;
			}
			
			Y1 = Y2-dim;
			
			for (int x = X1; x < X2; x+= dim) {
				if (x == X1)
					render(r, x, Y1, DIR.W, DIR.S, dirmask);
				else if (x + dim >= X2) {
					render(r, X2-dim, Y1, DIR.E, DIR.S, dirmask);
				}else {
					render(r, x, Y1, DIR.S, DIR.S, dirmask);
				}
			}
			
		}
		
		private void render(SPRITE_RENDERER r, int x, int y, DIR d1, DIR d2, int dirMask) {
			
			int m = (d1.mask() & dirMask) | (d2.mask() & dirMask);
			int i = toBox[m&0x0F];
			sheet.render(r, i, x, y);
	
		}
		
		public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2, int margin) {
			render(r, X1, X2, Y1, Y2, margin, -1);
		}
		
		public void render(SPRITE_RENDERER r, RECTANGLE body, int margin) {
			render(r, body.x1(), body.x2(), body.y1(), body.y2(), margin);
		}
		
		public void render(SPRITE_RENDERER r, RECTANGLE body, int margin, DIR d1, DIR d2) {
			render(r, body.x1(), body.x2(), body.y1(), body.y2(), margin, d1.mask()|d2.mask());
		}
		
		public void renderVertical(SPRITE_RENDERER r, int x1, int y1, int height) {
			int y2 = y1+height;
			while(y1 < y2) {
				if (y1+dim > y2)
					y1 = y2-dim;
				render(r, x1, y1, DIR.W, DIR.W, -1);
				y1 += dim;
			}
		}
		
		public void renderHorizontal(SPRITE_RENDERER r, int x1, int x2, int y1) {
			
			while(x1 <= x2-dim) {
				render(r, x1, y1, DIR.N, DIR.N, -1);
				x1 += dim;
			}
			render(r, x2-dim, y1, DIR.N, DIR.N, -1);
		}

	}

}