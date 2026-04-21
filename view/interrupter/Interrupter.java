package view.interrupter;

import snake2d.MButt;
import snake2d.Renderer;
import snake2d.util.datatypes.COORDINATE;
import util.gui.misc.GBox;
import view.main.VIEW.ViewSub;

public abstract class Interrupter {

	boolean persistent;
	boolean desturbingfuck;
	private boolean pinned;
	
	private boolean last;

	InterManager addManager;
	
	protected Interrupter(){
		this(false, false, false);
	}
	
	/**
	 * 
	 * @param manager
	 * @param persistent persists if other interrupter is put on top
	 * @param pinned keep if manager is cleared
	 */
	protected Interrupter(boolean persistent, boolean pinned){
		this(persistent, pinned, true);
	}
	
	protected Interrupter(boolean persistent, boolean pinned, boolean desturber){
		this.persistent = pinned || persistent;
		this.pinned = pinned;
		desturbingfuck = desturber;
	}
	
	protected final boolean show(ViewSub view){
		return show(view.uiManager);
	}
	
	protected final boolean show(InterManager manager){
		if (addManager == null) {
			manager.add(this);
			return true;
		}
		return false;
	}
	
	protected void hide() {
		if (addManager != null) {
			addManager.remove(this);
		}
	}
	
	protected void deactivateAction() {
		
	}
	
	protected void otherAdd(Interrupter other) {
		
	}
	
	protected boolean DoWhateverAndallowOthersToDoWhatever() {
		return true;
	}

	/**
	 * 
	 * @param mCoo
	 * @return true if this hover has been consumed. You will be clicked
	 */
	protected abstract boolean hover(COORDINATE mCoo, boolean mouseHasMoved);
	
	/**
	 * 
	 * @param button
	 * called if the mouse ha been pressed and you've answered true in hovered
	 * return true if the click is consumed, else other will be otherclicked.
	 */
	protected abstract void mouseClick(MButt button);
	
	
	/**
	 * a click! But not meant for you!
	 * @param button
	 * @return true if you consume the click e.g. close yourself
	 */
	protected boolean otherClick(MButt button){
		return false;
	}
	
	/**
	 * 
	 * @param mouseStillTime
	 * @return null of info to be displayed only called if hover() was true
	 */
	protected abstract void hoverTimer(GBox text);
	/**
	 * 
	 * @param ds
	 * @return should render next
	 */
	protected abstract boolean render(Renderer r, float ds);


	/**
	 * 
	 * @param ds
	 * @return if the next should update
	 */
	protected abstract boolean update(float ds);
	
	
	/**
	 * Called after each update/render tick
	 */
	protected void afterTick() {
		
	}
	
	public final boolean last() {
		return last;
	}
	
	public Interrupter lastSet() {
		this.last = true;
		return this;
	}
	
	public Interrupter persistantSet() {
		this.persistent = true;
		return this;
	}
	
	public Interrupter desturberSet() {
		this.desturbingfuck = true;
		return this;
	}
	
	final boolean isPersistent(){
		return persistent;
	}
	
	final boolean pinned(){
		return pinned;
	}
	
	public final Interrupter pin(){
		pinned = true;
		return this;
	}
	
	public final boolean isActivated(){
		return addManager != null;
	}
	
	public InterManager manager() {
		return addManager;
	}

	public boolean canSave() {
		return true;
	}
	
}
