package edu.ucsd.gwt2.modelview.client;

import com.google.gwt.animation.client.Animation;
import com.google.gwt.core.client.Duration;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class LoadingPanel extends PopupPanel
{
	private final Label text = new Label();
	private final Element bar;
	private final Anim animation = new Anim();
	private boolean animating = false;
	private double animation_start = -1;
	private double progress = 0;
	
	public LoadingPanel()
	{
		// Setup the panel
		super(false, true);
		this.getElement().setId("loading-box");
		VerticalPanel p = new VerticalPanel();
		this.add(p);

		// Create the top row: text
		p.add(this.text);
		
		// Create the bottom row: progress bar
		HTMLPanel meter = new HTMLPanel("<span style='width:0%'><span></span></span>");
		meter.addStyleName("meter");
		this.bar = meter.getElement().getFirstChildElement();
		p.add(meter);
	}
	
	public String getText() { return this.text.getText(); }
	public void setText(String text) { this.text.setText(text); }
	
	public double getProgress() { return this.progress; }
	public void setProgress(int current, int total) { this.setProgress(current*100.0/total); }
	public void setProgress(double percent)
	{
		double progress = Math.min(Math.max(percent, 0), 100);
		if (progress > this.progress)
		{
			double now = Duration.currentTimeMillis();
			if (!this.animating) // || this.animation_start + 1000 <= now)
			{
				this.animation_start = now;
				this.animating = true;
			}
			this.animation.run(1000, this.animation_start);
		}
		else
		{
			this.animation.cancel();
			this.animating = false;
			this.bar.getStyle().setWidth(progress, Unit.PCT);
		}
		this.progress = progress;
	}
	
	private class Anim extends Animation
	{
		@Override
		protected void onUpdate(double percent)
		{
			LoadingPanel.this.bar.getStyle().setWidth(LoadingPanel.this.progress * percent, Unit.PCT);
		}
	}
	
	public void show(String text) { this.text.setText(text); this.progress = 0.0; this.bar.getStyle().setWidth(0.0, Unit.PCT); super.center(); }
}
