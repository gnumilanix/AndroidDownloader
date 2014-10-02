package com.milanix.example.downloader.fragment;

import android.app.Service;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.milanix.example.downloader.R;
import com.milanix.example.downloader.fragment.abs.AbstractFragment;
import com.milanix.example.downloader.util.ToastHelper;

/**
 * This fragment contains browser and its related logic.
 */
public class BrowseFragment extends AbstractFragment implements
		OnEditorActionListener, OnClickListener {

	protected View rootView;

	private EditText tab_url;

	private ImageButton tab_favourite;

	private WebView tab_page;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_browse, container, false);

		onInit();
		initWebView();

		return rootView;
	}

	@Override
	protected void setUI() {
		tab_url = (EditText) rootView.findViewById(R.id.tab_url);
		tab_favourite = (ImageButton) rootView.findViewById(R.id.tab_favourite);
		tab_page = (WebView) rootView.findViewById(R.id.tab_page);
	}

	@Override
	protected void setListener() {
		tab_url.setOnEditorActionListener(this);
		tab_favourite.setOnClickListener(this);
	}

	@Override
	public String getLogTag() {
		return BrowseFragment.class.getSimpleName();
	}

	@Override
	public void onClick(View view) {
		if (view.getId() == R.id.tab_favourite) {
			ToastHelper
					.showToast(getActivity(), "Feature not yet implemeneted");
		}
	}

	@Override
	public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
		if (actionId == EditorInfo.IME_ACTION_GO) {
			if (view.getId() == R.id.tab_url) {
				InputMethodManager imm = (InputMethodManager) getActivity()
						.getSystemService(Service.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

				view.setCursorVisible(false);

				openUrl(tab_url.getText().toString());
				return true;
			}
		}
		return false;
	}

	/**
	 * THis method will initialize webview
	 */
	private void initWebView() {
		WebSettings settings = tab_page.getSettings();
		settings.setJavaScriptEnabled(true);
		tab_page.setScrollBarStyle(WebView.SCROLLBARS_OUTSIDE_OVERLAY);
		tab_page.setWebChromeClient(new ListenableWebChromeClient());
		tab_page.setWebViewClient(new ListenableWebViewClient());
	}

	/**
	 * This method will load given url
	 * 
	 * @param url
	 */
	private void openUrl(String url) {
		tab_page.loadUrl(url);
	}

	/**
	 * WebView client for listening to progress change
	 */
	class ListenableWebViewClient extends WebViewClient {
		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon) {
			Log.i(getLogTag(), "URL loading started");

			super.onPageStarted(view, url, favicon);
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			Log.i(getLogTag(), "URL loading completed");

			super.onPageFinished(view, url);
		}

	}

	/**
	 * WebView client for listening to progress change
	 */
	class ListenableWebChromeClient extends WebChromeClient {

		@Override
		public void onProgressChanged(WebView view, int progress) {
		}
	}

}
