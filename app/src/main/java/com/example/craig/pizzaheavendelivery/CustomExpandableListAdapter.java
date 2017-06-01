package com.example.craig.pizzaheavendelivery;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.opengl.Visibility;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import pizzaheaven.controllers.CustomerController;
import pizzaheaven.controllers.OrderController;
import pizzaheaven.models.Customer;
import pizzaheaven.models.Order;
import pizzaheaven.models.Session;
import pizzaheaven.models.Staff;

public class CustomExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> expandableListTitle;
    private LinkedHashMap<String, List<String>> expandableListDetail;
    public boolean plsUpdate = false;
    private Staff staff;
    private Button btnLaunchMap;
    private Button btnCallCustomer;
    private LinearLayout pnlCustomer;

    public CustomExpandableListAdapter(Context context, List<String> expandableListTitle,
                                       LinkedHashMap<String, List<String>> expandableListDetail, Staff staff) {
        this.context = context;
        this.expandableListTitle = expandableListTitle;
        this.expandableListDetail = expandableListDetail;
        this.staff = staff;
    }

    /*@Override
    public void onGroupExpanded(int group) {
        for (int i = 0; i < this.getGroupCount(); i++) {

        }
    }*/

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final String expandedListText = (String) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_item, null);
        }
        TextView expandedListTextView = (TextView) convertView
                .findViewById(R.id.expandedListItem);
        expandedListTextView.setText(expandedListText);
        Button btnDelivered = (Button) convertView.findViewById(R.id.btnDelivered);
        btnLaunchMap = (Button) convertView.findViewById(R.id.btnLaunchMap);
        Button btnFailedDelivery = (Button) convertView.findViewById(R.id.btnFailedDelivery);
        Button btnCallCustomer = (Button)convertView.findViewById(R.id.btnCallCustomer);
        String listTitle = expandableListTitle.get(listPosition);
        pnlCustomer = (LinearLayout)convertView.findViewById(R.id.pnlCustomer);
        String orderID = listTitle.split(Pattern.quote(" "))[2];
        final String[] postcode = new String[1];
        final String[] address = new String[1];
        Thread orderCheckThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Order order = ((OrderController) Session.get().getController("OrderController")).get(Integer.valueOf(orderID));
                Customer cust = null;
                for (Customer tempCust : ((CustomerController) Session.get().getController("CustomerController")).get()) {
                    if (tempCust.getCustomerID().equals(order.getCustomerID())) {
                        cust = tempCust;
                        break;
                    }
                }
                if (cust.getFirstName().contains("=")) cust = (Customer)Encryption.decrypt(cust);
                postcode[0] = cust.getDeliveryPostCode();
                address[0] = cust.getDeliveryLineOne() + " " + cust.getDeliveryLineTwo();
                if (order.getStatus().equals("Out For Delivery")) {
                    btnDelivered.setText("Delivered");
                    pnlCustomer.setVisibility(View.VISIBLE);
                    btnFailedDelivery.setVisibility(View.VISIBLE);
                    btnCallCustomer.setVisibility(View.VISIBLE);

                } else {
                    btnDelivered.setText("Claim Delivery");
                }
            }
        }, "orderCheckThread");
        orderCheckThread.start();
        while(orderCheckThread.isAlive()) { }
        final String[] customerNumber = new String[1];
        Thread getPhoneNumberThread = new Thread(new Runnable() {
            public void run() {
                Order order = ((OrderController) Session.get().getController("OrderController")).get(Integer.valueOf(orderID));
                Customer cust = null;
                for (Customer tempCust : ((CustomerController) Session.get().getController("CustomerController")).get()) {
                    if (tempCust.getCustomerID().equals(order.getCustomerID())) {
                        cust = tempCust;
                        break;
                    }
                }
                if (cust.getFirstName().contains("=")) cust = (Customer)Encryption.decrypt(cust);
                customerNumber[0] = cust.getPhoneNumber();

            }
        });
        getPhoneNumberThread.start();
        while(getPhoneNumberThread.isAlive()) { }
        btnCallCustomer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                context.startActivity(new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", customerNumber[0], null)));
            }
        });

        btnLaunchMap.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + URLEncoder.encode(address[0]) + "+" + URLEncoder.encode(postcode[0]));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                context.startActivity(mapIntent);
            }
        });

        btnDelivered.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (btnDelivered.getText().equals("Claim Delivery")) {
                    btnDelivered.setText("Delivered");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Order order = ((OrderController)Session.get().getController("OrderController")).get(Integer.valueOf(orderID));
                            order.setStatus("Out For Delivery");
                            order.setStaffID(staff.getStaffID());
                            ((OrderController)Session.get().getController("OrderController")).update(order);
                            plsUpdate = true;
                        }
                    }).start();
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Order order = ((OrderController)Session.get().getController("OrderController")).get(Integer.valueOf(orderID));
                            order.setStatus("Delivered");
                            expandableListTitle.remove(listTitle);
                            expandableListDetail.remove(listTitle);
                            ((OrderController)Session.get().getController("OrderController")).update(order);
                            plsUpdate = true;
                        }
                    }).start();
                }
            }
        });

        btnFailedDelivery.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Order order = ((OrderController)Session.get().getController("OrderController")).get(Integer.valueOf(orderID));
                        order.setStatus("Failed Delivery");
                        order.setStaffID("1");
                        expandableListTitle.remove(listTitle);
                        expandableListDetail.remove(listTitle);
                        ((OrderController)Session.get().getController("OrderController")).update(order);
                        plsUpdate = true;
                    }
                }).start();
            }
        });
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        return this.expandableListDetail.get(this.expandableListTitle.get(listPosition))
                .size();
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.expandableListTitle.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.expandableListTitle.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        String listTitle = expandableListTitle.get(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.list_group, null);
        }
        TextView listTitleTextView = (TextView) convertView
                .findViewById(R.id.listTitle);
        listTitleTextView.setTypeface(null, Typeface.BOLD);
        listTitleTextView.setText(listTitle);
        String orderID = listTitle.split(Pattern.quote(" "))[2];
        Thread orderCheckThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Order order = ((OrderController) Session.get().getController("OrderController")).get(Integer.valueOf(orderID));
                Customer cust = null;
                for (Customer tempCust : ((CustomerController) Session.get().getController("CustomerController")).get()) {
                    if (tempCust.getCustomerID().equals(order.getCustomerID())) {
                        cust = tempCust;
                        break;
                    }
                }
                if (cust.getFirstName().contains("=")) cust = (Customer)Encryption.decrypt(cust);
                if (order.getStatus().equals("Out For Delivery")) {
                    listTitleTextView.setBackgroundColor(Color.rgb(218, 66, 70));
                    listTitleTextView.setTextColor(Color.WHITE);
                } else {
                    listTitleTextView.setBackgroundColor(Color.WHITE);
                    listTitleTextView.setTextColor(Color.BLACK);
                }
            }
        }, "orderCheckThread");
        orderCheckThread.start();
        while(orderCheckThread.isAlive()) { }
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
