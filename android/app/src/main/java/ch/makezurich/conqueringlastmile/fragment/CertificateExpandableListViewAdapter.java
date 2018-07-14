package ch.makezurich.conqueringlastmile.fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.List;

import ch.makezurich.conqueringlastmile.R;

public class CertificateExpandableListViewAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<Certificate> certificates;

    public CertificateExpandableListViewAdapter(Context context, List<Certificate> certificates) {
        this.context = context;
        this.certificates = certificates;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.certificates.get(listPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = ((Certificate) getChild(listPosition, expandedListPosition)).toString();
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.cert_detail, null);
        }
        TextView expandedListTextView = (TextView) convertView
                .findViewById(R.id.certificateListItem);
        expandedListTextView.setText(expandedListText);
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.certificates.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.certificates.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle;
        Certificate cert = (Certificate) getGroup(listPosition);
        if (cert instanceof X509Certificate) {
            listTitle = ((X509Certificate) cert).getIssuerDN().getName();
        } else {
            listTitle = cert.getType();
        }
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.cert_detail, parent, false);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.certificateListItem);
        listTitleTextView.setText(listTitle);
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
