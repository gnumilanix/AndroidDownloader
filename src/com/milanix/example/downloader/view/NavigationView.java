package com.milanix.example.downloader.view;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.milanix.example.downloader.R;

/**
 * This is a navigation view for navigating between folder hierarchy
 * 
 * @author Milan
 * 
 */
public class NavigationView extends HorizontalScrollView {

	// Label gravity
	public static final int TOP = 0;
	public static final int BOTTOM = 1;
	public static final int CENTER = 2;

	public static final int DEFAULT_LABELPADDING = 16;
	public static final int DEFAULT_MARGINBEFORE = 0;
	public static final int DEFAULT_MARGINAFTER = 16;

	// Attributes
	private Drawable tabBackground;
	private int tabMarginBefore;
	private int tabMarginAfter;
	private int labelColor;
	private int labelGravity;
	private int labelPadding;

	private Context context;
	private LinearLayout root;

	private ArrayList<NavigationTab> navigationTabs = new ArrayList<NavigationTab>();

	/**
	 * This is the default constructor of this view
	 * 
	 * @param context
	 *            is the base application context
	 * @param attrs
	 *            is the attributes
	 * @param defStyleAttr
	 */
	public NavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);

		TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
				R.styleable.navigationview, 0, 0);

		try {
			tabBackground = a
					.getDrawable(R.styleable.navigationview_tab_background);

			tabMarginBefore = a.getDimensionPixelSize(
					R.styleable.navigationview_tab_marginbefore,
					DEFAULT_MARGINBEFORE);
			tabMarginAfter = a.getDimensionPixelSize(
					R.styleable.navigationview_tab_marginafter,
					DEFAULT_MARGINAFTER);

			labelColor = a.getColor(R.styleable.navigationview_label_color,
					Color.parseColor("#000000"));
			labelGravity = a.getInteger(
					R.styleable.navigationview_label_gravity, CENTER);
			labelPadding = a.getDimensionPixelSize(
					R.styleable.navigationview_label_padding,
					DEFAULT_LABELPADDING);
		} finally {
			a.recycle();
		}

		init(context);
	}

	/**
	 * This is the default constructor of this view
	 * 
	 * @param context
	 *            is the base application context
	 * @param attrs
	 *            is the attributes
	 */
	public NavigationView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	/**
	 * This is the default constructor of this view
	 * 
	 * @param context
	 *            is the base application context
	 * @param attrs
	 *            is the attributes
	 */
	public NavigationView(Context context) {
		this(context, null, 0);
	}

	/**
	 * This method will init required params
	 * 
	 * @param context
	 *            is the base application context
	 */
	private void init(Context context) {
		this.context = context;

		root = new LinearLayout(context);
		root.setOrientation(LinearLayout.HORIZONTAL);

		addView(root, new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT));
	}

	/**
	 * This method will apply LabelGravity to this view
	 * 
	 * @param labelGravity
	 *            the labelGravity to set
	 */
	public void setLabelGravity(int labelGravity) {
		this.labelGravity = labelGravity;

		invalidate();
		requestLayout();
	}

	/**
	 * This method will return LabelGravity applied to this view
	 * 
	 * @return
	 */
	public int getLabelGravity() {
		return labelGravity;

	}

	/**
	 * @return the tabBackground
	 */
	public Drawable getTabBackground() {
		return tabBackground;
	}

	/**
	 * @param tabBackground
	 *            the tabBackground to set
	 */
	public void setTabBackground(Drawable tabBackground) {
		this.tabBackground = tabBackground;

		invalidate();
		requestLayout();
	}

	/**
	 * @return the labelColor
	 */
	public int getLabelColor() {
		return labelColor;
	}

	/**
	 * @param labelColor
	 *            the labelColor to set
	 */
	public void setLabelColor(int labelColor) {
		this.labelColor = labelColor;

		invalidate();
		requestLayout();
	}

	/**
	 * This method will return navigation tabs in this view
	 * 
	 * @return list of navigation tabs
	 */
	public ArrayList<NavigationTab> getNavigationTabs() {
		return navigationTabs;
	}

	/**
	 * This method will add navigation tab to this view
	 * 
	 * @param navigationTab
	 */
	public void addNavigationTab(final NavigationTab navigationTab) {
		if (null != root && null != navigationTabs && null != navigationTab) {
			final TextView tab = new TextView(context);
			tab.setId(navigationTabs.size());
			tab.setTextColor(labelColor);

			if (null != navigationTab.label)
				tab.setText(navigationTab.label);

			if (null != tabBackground)
				tab.setBackground(tabBackground);

			tab.setPadding(labelPadding, labelPadding / 2, labelPadding,
					labelPadding / 2);

			switch (getLabelGravity()) {
			case TOP:
				tab.setGravity(Gravity.TOP);

				break;
			case BOTTOM:
				tab.setGravity(Gravity.BOTTOM);

				break;
			default:
				tab.setGravity(Gravity.CENTER_VERTICAL);

				break;
			}

			LinearLayout.LayoutParams tabParams = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
			tabParams.setMargins(tabMarginBefore, 0, tabMarginAfter, 0);

			tab.setLayoutParams(tabParams);

			if (null != navigationTab.callback) {
				tab.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View view) {
						navigationTab.callback.OnNavigationTabClicked(
								navigationTab, navigationTab.attachedObject);
					}
				});
			}

			navigationTabs.add(navigationTab);

			root.addView(tab, tabParams);
		}
	}

	/**
	 * This is a navigation tab item
	 * 
	 * @author Milan
	 * 
	 */
	public static class NavigationTab {
		// Label to be shown in the tab
		public String label;

		// Callback triggered when tab is clicked
		public OnNavigationTabClickListener callback;

		// Object to be passed with callback
		public Object attachedObject;

		public NavigationTab(String label,
				OnNavigationTabClickListener callback, Object attachedObject) {
			this.label = label;
			this.callback = callback;
			this.attachedObject = attachedObject;
		}
	}

	/**
	 * This is an interface to allow consumers of NavigationView to listen to
	 * click on the specific tab
	 * 
	 * @author Milan
	 * 
	 */
	public static interface OnNavigationTabClickListener {
		/**
		 * This method will be called when navigation tab is clicked.
		 * 
		 * @param navigationTab
		 *            is the NavigationTab click was performed. Null if not
		 *            passed
		 * 
		 * @param attachedObject
		 *            is the object attached with this tab. Null if not passed
		 */
		public void OnNavigationTabClicked(NavigationTab navigationTab,
				Object attachedObject);
	}

}
