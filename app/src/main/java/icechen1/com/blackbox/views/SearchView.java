package icechen1.com.blackbox.views;

import android.view.View;
import android.widget.EditText;

import icechen1.com.blackbox.R;

/**
 * This view is a ViewHolder of the SearchView
 *
 * @author Pedro Paulo Amorim
 *
 */
public class SearchView {

    private SearchViewCallback searchViewCallback;
    private EditText searchEditText;

    public View getView(View view, final SearchViewCallback searchViewCallback) {
        this.searchViewCallback = searchViewCallback;
        this.searchEditText = (EditText) view.findViewById(R.id.search_edit_text);
        view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (searchViewCallback != null) {
                    searchViewCallback.onCancelClick();
                }
            }
        });
        return view;
    }

    public String getText() {
        return searchEditText.getText().toString();
    }

    public interface SearchViewCallback {
        void onCancelClick();
    }

}