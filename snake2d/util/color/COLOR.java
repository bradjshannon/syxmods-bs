package snake2d.util.color;

import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sprite.SPRITE;
import snake2d.util.sprite.TextureCoords;


public interface COLOR extends SPRITE, RGB{

	public final static COLOR WHITE100 = new ColorImp(127, 127, 127);
	public final static COLOR WHITE120 = new ColorImp(150, 150, 150);
	public final static COLOR WHITE150 = new ColorImp(190, 190, 190);
	public final static COLOR WHITE200 = new ColorImp(255, 255, 255);
	public final static COLOR WHITE85 = new ColorImp(109, 109, 109);
	public final static COLOR WHITE65 = new ColorImp(80, 80, 80);
	public final static COLOR WHITE50 = new ColorImp(64, 64, 64);
	public final static COLOR WHITE35 = new ColorImp(45, 45, 45);
	public final static COLOR WHITE30 = new ColorImp(38, 38, 38);
	public final static COLOR WHITE25 = new ColorImp(31, 31, 31);
	public final static COLOR WHITE20 = new ColorImp(26, 26, 26);
	public final static COLOR WHITE15 = new ColorImp(19, 19, 19);
	public final static COLOR WHITE10 = new ColorImp(13, 13, 13);
	public final static COLOR WHITE05 = new ColorImp(7, 7, 7);


	public final static COLOR BROWN = new ColorImp(72, 58, 33);
	public final static COLOR BLACK = new ColorImp(0, 0, 0);
	public final static COLOR RED50 = new ColorImp(64, 0, 0);
	public final static COLOR RED100 = new ColorImp(127, 0, 0);
	public final static COLOR REDISH = new ColorImp(127, 40, 40);
	public final static COLOR RED200 = new ColorImp(255, 0, 0);
	public final static COLOR RED2RED = new ColorShifting(RED50, RED100);
	public final static COLOR GREEN40 = new ColorImp(0, 51, 0);
	public final static COLOR GREEN80 = new ColorImp(0, 102, 0);
	public final static COLOR GREEN90 = new ColorImp(0, 115, 0);
	public final static COLOR GREEN100 = new ColorImp(0, 128, 0);
	public final static COLOR GREEN200 = new ColorImp(0, 255, 0);
	public final static COLOR GREENISH80 = new ColorImp(90, 120, 90);
	public final static COLOR GREENISH200 = new ColorImp(200, 255, 200);
	public final static COLOR ORANGE100 = new ColorImp(127,53,0);
	public final static COLOR ORANGE150 = new ColorImp(127,72,72);
	public final static COLOR YELLOW100 = new ColorImp(127,127,0);
	public final static COLOR BLUE50 = new ColorImp(0,0,64);
	public final static COLOR BLUEDARK = new ColorImp(10,10,24);
	public final static COLOR BLUE100 = new ColorImp(0,0,127);
	public final static COLOR NYAN100 = new ColorImp(0,127,127);
	public final static COLOR BLUEISH = new ColorImp(48,48,127);
	public final static COLOR PURPLE = new ColorImp(127, 0, 127);
	public final static COLOR PURPLISH = new ColorImp(127, 70, 127);
	
	public static final COLOR GREEN2RED = new ColorShifting(ColorImp.RED100, ColorImp.RED100);
	public static final COLOR GREEN2GREEN = new ColorShifting(ColorImp.GREEN40, ColorImp.GREEN100);
	public static final COLOR GREENISH = new ColorShifting(GREENISH80, GREENISH200);
	public static final COLOR WHITE2WHITE = new ColorShifting(WHITE50, WHITE200);
	public static final COLOR WHITE702WHITE100 = new ColorShifting(WHITE65, WHITE100);
	public static final COLOR WHITE202WHITE100 = new ColorShifting(WHITE20, WHITE100).setSpeed(2.5);
	public static final COLOR WHITE15WHITE50 = new ColorShifting(WHITE15, WHITE50);
	public static final COLOR WHITE120_2_WHITE150 = new ColorShifting(WHITE120, WHITE150);
	public static final COLOR BLACK2WHITE = new ColorShifting(BLACK, WHITE100);
	public static final COLOR BLUE2BLUE = new ColorShifting(BLUE50, BLUE100);
	
	public final static COLOR DARK_GREEN = new ColorImp(7, 36, 2);
	public final static COLOR MEDIUM_GREEN = new ColorImp(7, 36, 2);
	public final static COLOR MEDIUM_BROWN = new ColorImp(26, 46, 2);
	public final static COLOR DARK_BROWN = new ColorImp(26, 30, 2);
	
	public final static LIST<COLOR> UNIQUE = new ArrayList<>(
			new ColorImp(255, 179, 0).shade(0.5),
			new ColorImp(128, 62, 117).shade(0.5),
			new ColorImp(255, 104, 0).shade(0.5),
			new ColorImp(166, 189, 215).shade(0.5),
			new ColorImp(193, 0, 32).shade(0.5),
			new ColorImp(206, 162, 98).shade(0.5),
			new ColorImp(129, 112, 102).shade(0.5),
			new ColorImp(0, 125, 52).shade(0.5),
			new ColorImp(246, 118, 142).shade(0.5),
			new ColorImp(0, 83, 138).shade(0.5),
			new ColorImp(255, 122, 92).shade(0.5),
			new ColorImp(83, 55, 122).shade(0.5),
			new ColorImp(255, 142, 0).shade(0.5),
			new ColorImp(179, 40, 81).shade(0.5),
			new ColorImp(244, 200, 0).shade(0.5),
			new ColorImp(127, 24, 13).shade(0.5),
			new ColorImp(147, 170, 0).shade(0.5),
			new ColorImp(89, 51, 21).shade(0.5),
			new ColorImp(241, 58, 19).shade(0.5),
			new ColorImp(35, 44, 22).shade(0.5)
			);
	

	
	default public void bind(){
		CORE.renderer().setColor(this);
	}
	
	public static void unbind(){
		CORE.renderer().setNormalColor();
	}
	
	public byte red();
	public byte green();
	public byte blue();

	public default int getComp(int comp) {
		switch(comp) {
		case 0 : return this.red()&0x0FF;
		case 1 : return this.green()&0x0FF;
		case 2 : return this.blue()&0x0FF;
		default: throw new RuntimeException(""+comp);
		}
	}
	
	public default ColorImp shade(double s) {
		int r = red() & 0x0FF;
		int g = green() & 0x0FF;
		int b = blue() & 0x0FF;
		return new ColorImp((int)(r*s), (int)(g*s), (int)(b*s));
	}
	
	public default ColorImp makeSaturated(double amount) {
		return new ColorImp().set(this).saturateSelf(amount);
		
	}
	

	public static ColorImp[] interpolate(COLOR c, COLOR c2, int amount) {
		double r = Byte.toUnsignedInt(c.red());
		double g = Byte.toUnsignedInt(c.green());
		double b = Byte.toUnsignedInt(c.blue());

		double dr = (double)(Byte.toUnsignedInt(c2.red())-r)/(amount-1);
		double dg = (double)(Byte.toUnsignedInt(c2.green())-g)/(amount-1);
		double db = (double)(Byte.toUnsignedInt(c2.blue())-b)/(amount-1);
		
		ColorImp[] res = new ColorImp[amount];
		
		for (int i = 0; i < amount; i++) {
			res[i] = new ColorImp((byte)r, (byte)g, (byte)b);
			r += dr;
			g += dg;
			b += db;
		}
		
		return res;
		
	}
	
	default void render(SPRITE_RENDERER r, RECTANGLE rec, int margin) {
		render(r, rec.x1()-margin, rec.x2()+margin, rec.y1()-margin, rec.y2()+margin);
	}
	
	default void render(SPRITE_RENDERER r, int x, int y, int w, int h, int margin) {
		render(r, x-margin, x+w+margin, y-margin, y+h+margin);
	}
	
	default void render(SPRITE_RENDERER r, int x, int y, int w, int h, int marginX, int marginY) {
		render(r, x-marginX, x+w+marginX, y-marginY, y+h+marginY);
	}
	
	default void renderFrame(SPRITE_RENDERER r, int x1, int x2, int y1, int y2, int m, int thickness) {
		x1 -= m;
		x2 += m;
		y1 -= m;
		y2 += m;
		render(r, x1, x1+thickness, y1, y2);
		render(r, x2-thickness, x2, y1, y2);
		render(r, x1+thickness, x2-thickness, y1, y1+thickness);
		render(r, x1+thickness, x2-thickness, y2-thickness, y2);
	}
	
	default void renderFrame(SPRITE_RENDERER r, RECTANGLE body, int m, int thickness) {
		renderFrame(r, body.x1(), body.x2(), body.y1(), body.y2(), m, thickness);
	}

	public default SPRITE makeSprite(int width, int height) {
		return new SPRITE() {
			
			@Override
			public int width() {
				return width;
			}
			
			@Override
			public int height() {
				return height;
			}
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				COLOR.this.render(r, X1, X2, Y1, Y2);
			}
		};
	}
	
	public default SPRITE makeFrame(int width, int height, int w) {
		return new SPRITE() {
			
			@Override
			public int width() {
				return width;
			}
			
			@Override
			public int height() {
				return height;
			}
			
			@Override
			public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
				COLOR.this.renderFrame(r, X1, X2, Y1, Y2, 0, w);
			}
		};
	}
	
	public static COLOR[] generateUnique(int min, int amount, boolean ran) {
		
		COLOR[] cols = new COLOR[amount];
		final int rM = (int) Math.pow(amount, 1.0/3.0);
		final int gM = rM;
		final int bM = (int) Math.ceil((double)amount/(rM*gM));
		
		double delta = 127-min;
		
		final double rD = delta/rM;
		final double gD = delta/gM;
		final double bD = delta/bM;
		
		int in = 0;
		outer:
		for (int r = 0; r < rM; r++) {
			for (int g = 0; g < gM; g++) {
				for (int b = 0; b < bM; b++) {
					if (in >= cols.length)
						break outer;
					ColorImp i = new ColorImp();
					i.setRed(min + (int) (rD/2 + r*rD));
					i.setGreen(min + (int) (gD/2 + g*gD));
					i.setBlue(min + (int) (bD/2 + b*bD));
					cols[in++] = i;
				}
			}
		}
		
		for (int i = 0; i < cols.length; i++) {
			int k = RND.rInt(cols.length);
			COLOR n = cols[i];
			cols[i] = cols[k];
			cols[k] = n;
		}
		
		
		return cols;
	}
	
	public static boolean equals(COLOR a, COLOR c) {
		if (a == null || c == null)
			return false;
		return c.red() == a.red() && c.green() == a.green() && c.blue() == a.blue();
	}
	
	public static COLOR[] generateUnique2(int min, int amount, boolean ran) {
		
		
		
		
		final int MAX = amount;
		
		final COLOR[] cols = new COLOR[MAX];
		
		int am = 0;
		int div = 2;
		double delta = 127-min;
		
		while(am < MAX) {
			for (int dr = 0; dr < div; dr++) {
				for (int dg = 0; dg < div; dg++) {
					for (int db = 0; db < div; db++) {
						if (dr == dg && dr == db) {
							continue;
						}
						int r = (int) (min+dr*delta/(div-1));
						int g = (int) (min+dg*delta/(div-1));
						int b = (int) (min+db*delta/(div-1));
						if (am < MAX) {
							cols[am] = new ColorImp(r, g, b);
						}
						am++;
						
					}
					
					
				}
				
			}
			div++;
			
		}
		
		if (ran) {
			for (int i = 0; i < cols.length; i++) {
				int k = RND.rInt(cols.length);
				COLOR n = cols[i];
				cols[i] = cols[k];
				cols[k] = n;
			}
		}
		

		
		
		return cols;
	}
	

	
}
