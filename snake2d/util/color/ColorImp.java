package snake2d.util.color;

import java.io.IOException;
import java.io.Serializable;

import snake2d.CORE;
import snake2d.SPRITE_RENDERER;
import snake2d.util.datatypes.RECTANGLE;
import snake2d.util.file.FileGetter;
import snake2d.util.file.FilePutter;
import snake2d.util.file.Json;
import snake2d.util.file.SAVABLE;
import snake2d.util.misc.CLAMP;
import snake2d.util.rnd.RND;
import snake2d.util.sets.ArrayList;
import snake2d.util.sets.LIST;
import snake2d.util.sets.LinkedList;
import snake2d.util.sprite.TextureCoords;


public class ColorImp implements COLOR, Serializable, SAVABLE{

	public static final ColorImp TMP = new ColorImp(); 
	
	private static final long serialVersionUID = 1L;
	private final static TextureCoords texture = new TextureCoords();
	private static short width;
	private static short height;
	
	/**
	 * called once to specify where white is on spritesheet
	 * @param wX1
	 * @param wX2
	 * @param wY1
	 * @param wY2
	 */
	public static void setSPRITE(int wX1, int wY1, int w, int h){
		texture.get(wX1, wY1, w, h);
		width = (short) w;
		height = (short) h;
	}

	
	private byte red;
	private byte green;
	private byte blue;
	
	public ColorImp(){
		this(127,127,127);
	}
	
	public ColorImp(int red, int green, int blue){
		
		this.setRed(red);
		this.setGreen(green);
		this.setBlue(blue);
		
	}
	
	public ColorImp(Json json) {
		this(json, "COLOR");
	}
	
	public static LIST<ColorImp> cols(Json json){
		return cols(json, "COLOR");
	}

	public static LIST<ColorImp> cols(Json json, String key){
		if (!json.has(key))
			throw new RuntimeException();
		if (json.jsonIs(key)) {
			json = json.json(key);
			if (json.has("R") && json.has("B") && json.has("G"))
				return new ArrayList<>(new ColorImp(json, key));
			else {
				COLOR from = new ColorImp(json, "FROM");
				COLOR to = new ColorImp(json, "TO");
				return new ArrayList<ColorImp>(COLOR.interpolate(from, to, json.i("GENERATE", 0, 1024)));
			}
		}else if (json.jsonsIs(key)) {
			LinkedList<ColorImp> cols = new LinkedList<>();
			for (Json j : json.jsons(key))
				cols.add(new ColorImp(j));
			return new ArrayList<ColorImp>(cols);
			
		}else if (json.arrayIs(key)) {
			String[] ss = json.values(key);
			ArrayList<ColorImp> res = new ArrayList<ColorImp>(ss.length);
			for (int i = 0; i < ss.length; i++) {
				ColorImp col = new ColorImp();
				col.set(ss[i], json);
				res.add(col);
			}
			return res;
		}else {
			String s = json.value(key);
			return new ArrayList<>(new ColorImp().set(s, json));
		}
		
		
	}
	
	public ColorImp(Json json, String key) {
		if (json.has(key) && json.jsonIs(key)) {
			json = json.json(key);
		}
		
		if (json.has("R") && json.has("G") && json.has("B")) {
			set(json.i("R", 0, 511)/2, json.i("G", 0, 511)/2, json.i("B", 0, 511)/2);
		}else {
			String v = json.value(key);
			set(v, json);
		}
		
	}

	public ColorImp set(String v, Json error) {

			
		
		if (v.indexOf("_") < 0)
			error.error("Wrong format of color. Should be RED_GREEN_BLUE", v);
		String[] vv = v.split("_");
		if (vv.length != 3)
			error.error("Wrong format of color. Should be RED_GREEN_BLUE, where RED, GREEN and BLUE is an integer 0-255. eg 129_12_0", v);
		int[] cols = new int[3];
		for (int i = 0; i < cols.length; i++) {
			try {
				cols[i] = Integer.parseInt(vv[i]);
			}catch(Exception e) {
				error.error("Wrong format of color. Should be RED_GREEN_BLUE, where RED, GREEN and BLUE is an integer 0-255. eg 129_12_0", v);
			}
		}
		
		set(cols[0]/2, cols[1]/2, cols[2]/2);
		return this;
	}
	
	public ColorImp(COLOR c){
		set(c);
	}
	
	public ColorImp set(COLOR c){
		if (c instanceof ColorShifting) {
			c.bind();
			COLOR.unbind();
		}
		this.red = c.red();
		this.blue = c.blue();
		this.green = c.green();
		return this;
	}
	
	public static void unBind(){
		CORE.renderer().setNormalColor();
	}
	
	@Override
	public byte red() {
		return red;
	}

	public ColorImp set(int r, int g, int b) {
		setRed(r);
		setGreen(g);
		setBlue(b);
		return this;
	}
	
	public ColorImp setAll(int i) {
		setRed(i);
		setGreen(i);
		setBlue(i);
		return this;
	}
	
	public ColorImp setRed(int red) {
		this.red = (byte) red;
		return this;
	}

	@Override
	public byte green() {
		return green;
	}

	public ColorImp setGreen(int green) {
		this.green = (byte) green;
		return this;
	}

	@Override
	public byte blue() {
		return blue;
	}

	public ColorImp setBlue(int blue) {
		this.blue = (byte) blue;
		return this;
	}

	public ColorImp setComp(int comp, int c) {
		switch(comp) {
		case 0 : setRed(c); break;
		case 1 : setGreen(c); break;
		case 2 : setBlue(c); break;
		default: throw new RuntimeException(""+comp);
		}
		return this;
	}
	

	
	
	public ColorImp setAmount(double amount, double max){
		blue = 0;
		double ratio = (amount/max);
		
		if (ratio < 0.5f){
			green = (byte) (128f *ratio*2f);
			red = (byte)128;
		}else if(ratio <= 1){
			if (ratio > 1f){
				ratio = 1f;
			}
			green = (byte)128;
			red = (byte)(128 - ((ratio-0.5f)*2f*128f));
		}else{
			if (ratio > 2f){
				ratio = 2f;
			}
			red = 0;
			green = (byte)128;
			blue = (byte) ((ratio-1)*128);
		}
		return this;
	}
	
	@Override
	public int width() {
		return width;
	}

	@Override
	public int height() {
		return height;
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int X2, int Y1, int Y2) {
		bind();
		r.renderSprite(X1, X2, Y1, Y2, texture);
		//white.render(X1, X2, Y1, Y2);
		unBind();
	}

	@Override
	public void render(SPRITE_RENDERER r, int X1, int Y1) {
		bind();
		r.renderSprite(X1, X1+width, Y1, Y1+width, texture);
		unBind();
	}
	
	@Override
	public String toString() {
		return Byte.toUnsignedInt(red) + "_" + Byte.toUnsignedInt(green) + "_" + Byte.toUnsignedInt(blue);
		
	}
	
	public String toString2() {
		return Byte.toUnsignedInt(red)*2 + "_" + Byte.toUnsignedInt(green)*2 + "_" + Byte.toUnsignedInt(blue)*2;
	}
	
	@Override
	public void render(SPRITE_RENDERER r, RECTANGLE rec) {
		render(r, rec.x1(), rec.x2(), rec.y1(), rec.y2());
	}

	public ColorImp interpolate(COLOR c1, COLOR c2, double part) {

		part = CLAMP.d(part, 0, 1);
		
		if (Double.isNaN(part))
			part = 0;
		
		int r = (int) Math.round((c1.red()& 0x0FF)*(1.0-part) + (c2.red()& 0x0FF)*part);
		int g = (int) Math.round((c1.green()& 0x0FF)*(1.0-part) + (c2.green()& 0x0FF)*part);
		int b = (int) Math.round((c1.blue()& 0x0FF)*(1.0-part) + (c2.blue()& 0x0FF)*part);
		
		setRed(r);
		setGreen(g);
		setBlue(b);
		return this;
	}
	
	public ColorImp interpolate(LIST<? extends COLOR> cols, double part) {

		if (cols.size() <= 0)
			return this;
		
		double dc = part*(cols.size()-1);
		int di = (int) dc;
		dc -= di;
		int dn = Math.min(di+1,cols.size()-1);
		interpolate(cols.getC(di), cols.getC(dn), dc);
		return this;
	}
	
	
	public void multiply(COLOR other) {
		
		double m = 127;
		double i = (double)1.0/m;
		
		double r = (red()& 0x0FF)*i;
		double g = (green()& 0x0FF)*i;
		double b = (blue()& 0x0FF)*i;
		
		double r1 = (other.red()& 0x0FF)*i;
		double g1 = (other.green()& 0x0FF)*i;
		double b1 = (other.blue()& 0x0FF)*i;
		
		setRed(CLAMP.i((int) ((r*r1)*m), 0, 255));
		setGreen(CLAMP.i((int) ((g*g1)*m), 0, 255));
		setBlue(CLAMP.i((int) ((b*b1)*m), 0, 255));
		
	}
	
	public ColorImp  shadeSelf(double shade) {
		set((int)((red()&0x0FF)*shade), (int)((green()&0x0FF)*shade), (int)((blue()&0x0FF)*shade));
		return this;
	}
	
	public ColorImp setBrightnessSelf(double shade) {
		int hi = red()&0x0FF;
		hi = Math.max(hi, green()&0x0FF);
		hi = Math.max(hi, blue()&0x0FF);
		
		int sh = (int) (shade*127);
		sh = CLAMP.i(sh, 0, 255);
		
		double d = 1.0 + (sh-hi)/255.0;
		
		setRed(CLAMP.i((int)((red()&0x0FF)*d), 0, 255));
		setGreen(CLAMP.i((int)((green()&0x0FF)*d), 0, 255));
		setBlue(CLAMP.i((int)((blue()&0x0FF)*d), 0, 255));
		return this;
	}
	
	public ColorImp setMinBrightnessSelf(double shade) {
		
		setBrightnessSelf(shade);
		
		int r = red%0x0FF;
		int g = green&0x0FF;
		int b = blue&0x0FF;
		
		double tot = r+g+b;
		tot /= 127*3;
		
		if (tot >= shade)
			return this;
		
		shade -=tot;
		shade*=127*3;
		
		if (r < 127) {
			int am = (int) (shade/3);
			am = (int) CLAMP.d(am, 0, 127-r);
			r += am;
			shade -= am;
		}
		
		if (g < 127) {
			int am = (int) (shade/2);
			am = (int) CLAMP.d(am, 0, 127-g);
			g += am;
			shade -= am;
		}
		
		int am = (int) (shade);
		am = (int) CLAMP.d(am, 0, 127-b);
		b += am;
		
		set(r, g, b);
		
		setRed(CLAMP.i(r, 0, 255));
		setGreen(CLAMP.i(g, 0, 255));
		setBlue(CLAMP.i(b, 0, 255));
		return this;
	}
	
	public ColorImp  add(int am) {
		set((int)((red()&0x0FF)+am), (int)((green()&0x0FF)+am), (int)((blue()&0x0FF)+am));
		return this;
	}
	
	public ColorImp saturateSelf(double amount) {
		
		double r = (red() & 0x0FF);
		double g = (green() & 0x0FF);
		double b = (blue() & 0x0FF);
		

		double min = 255.0;
		double max = 0;
		
		if (r < min)
			min = r;
		if (r > max)
			max = r;
		if (g < min)
			min = g;
		if (g > max)
			max = g;
		if (b < min)
			min = b;
		if (b > max)
			max = b;
		
		double lum = (min+max)/2.0;
		
		
		int red = (int) (lum + (r-lum)*amount);
		int green = (int) (lum + (g-lum)*amount);
		int blue = (int) (lum + (b-lum)*amount);
		set(red, green, blue);
		return this;
		
	}

	@Override
	public void renderTextured(TextureCoords texture, int X1, int X2, int Y1, int Y2) {
		CORE.renderer().renderTextured(X1, X2, Y1, Y2, 
				texture, ColorImp.texture);
		
	}

	@Override
	public void save(FilePutter file) {
		file.i((red&0x0FF) | ((green<<8)&0x0FF00) | ((blue<<16)&0x0FF0000));
	}

	@Override
	public void load(FileGetter file) throws IOException {
		int i = file.i();
		red = (byte) (i & 0x0FF);
		green = (byte) ((i>>8) & 0x0FF);
		blue = (byte) ((i>>16) & 0x0FF);
	}

	@Override
	public TextureCoords texture() {
		return texture;
	}
	
	@Override
	public void clear() {
		
		
	}

	public void randomize(double d) {
		red = (byte) CLAMP.i((int) (red+RND.rFloat()*d*255), 0, 255);
		green = (byte) CLAMP.i((int) (green+RND.rFloat()*d*255), 0, 255);
		blue = (byte) CLAMP.i((int) (blue+RND.rFloat()*d*255), 0, 255);
	}
	
	private static final double ii = 1.0/0x0FF;
	
	@Override
	public double r() {
		return (red & 0x0FF)*ii;
	}
	
	@Override
	public double g() {
		return (green & 0x0FF)*ii;
	}
	
	@Override
	public double b() {
		return (blue & 0x0FF)*ii;
	}
}
