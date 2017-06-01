package com.example.craig.pizzaheavendelivery;

import android.content.Intent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.AbsListView;
import android.widget.ExpandableListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.os.AsyncTask;

import pizzaheaven.controllers.CustomerController;
import pizzaheaven.controllers.DrinksController;
import pizzaheaven.controllers.OrderController;
import pizzaheaven.controllers.OrderedItemController;
import pizzaheaven.controllers.PizzaController;
import pizzaheaven.controllers.SideController;
import pizzaheaven.controllers.StaffController;
import pizzaheaven.models.Customer;
import pizzaheaven.models.Drink;
import pizzaheaven.models.Order;
import pizzaheaven.models.OrderItem;
import pizzaheaven.models.Pizza;
import pizzaheaven.models.Session;
import pizzaheaven.models.Side;
import pizzaheaven.models.Staff;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    ExpandableListView lstOrders;
    private boolean     timerTaskRunning = false;
    private int         counter            = 0;
    private TimerTask timerTask        = null;
    private Order[] cachedOrders = null;
    private Customer[] cachedCustomers = null;
    private OrderItem[] cachedOrderItems = null;
    private Pizza[] cachedPizzas = null;
    private Drink[] cachedDrinks = null;
    private Side[] cachedSides = null;
    private CustomExpandableListAdapter adapter = null;
    private LinkedHashMap<String, List<String>> expandableListDetail = new LinkedHashMap<String, List<String>>();
    private List<String> titles = new ArrayList<String>();
    private SwipeRefreshLayout swipeRefresh;
    private boolean updateRequired = false;
    private List<Integer> viewsToRemove;
    private ScrollView lstScroll;
    private TextView txtCurrentUser;
    private int position;
    private int selected;
    private Staff staff;

    private boolean resetIdleTimer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("Activity created");
        setContentView(R.layout.activity_main);
        lstOrders = (ExpandableListView)findViewById(R.id.lstOrders);
        swipeRefresh = (SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
        txtCurrentUser = (TextView)findViewById(R.id.txtCurrentUser);
        viewsToRemove = new ArrayList<Integer>();
        selected =0;
        swipeRefresh.setOnRefreshListener(
            new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    System.out.println("onRefresh called from SwipeRefreshLayout");
                    updateRequired = true;
                    while (updateRequired) {
                    }
                    swipeRefresh.setRefreshing(false);
                }
            }
        );

        Intent intent = getIntent();
        String staffID  = intent.getStringExtra(LoginActivity.STAFFID);
        Thread workerThread = new Thread(new Runnable() {
           @Override
            public void run() {
               staff = ((StaffController)Session.get().getController("StaffController")).get(staffID);
               staff = (Staff)Encryption.decrypt(staff);
           }
        });
        workerThread.start();
        while(workerThread.isAlive()) {

        }
        txtCurrentUser.setText("Welcome back: " + staff.getFirstName() + " " + staff.getSurname());


        lstOrders.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {
            @Override
            public void onGroupExpand(int i) {
                selected = i;
                if (adapter != null) {
                    for (int j = 0; j < adapter.getGroupCount(); j++) {
                        if (j != i)
                            lstOrders.collapseGroup(j);
                    }
                }
            }
        });

        lstOrders.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                position = firstVisibleItem;
                /*if (resetIdleTimer == false)
                    resetIdleTimer = true;*/

            }
        });
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        timerTask        = new TimerTask();
        timerTaskRunning = true;
        timerTask.execute(null, null, null);
        System.out.println("Trying to display orders on resume...");
        displayOrders();
        Log.i("Thread Demo", "Activity resumed");
    }

    public void displayOrders() {

        for (int i = 0; i < viewsToRemove.size(); i++) {
            lstOrders.removeViewAt(i);
        }
        if (cachedOrders != null && cachedCustomers != null) {
            for (Order order : cachedOrders) {
                //we want to show the orders that are either ready for delivery or are out for delivery by the driver in question.
                if (order.getStatus().equals("Ready For Delivery") || (order.getStatus().equals("Out For Delivery") && order.getStaffID().equals(staff.getStaffID()))) {
                    for (Customer customer : cachedCustomers) {
                        if (customer.getCustomerID().equals(order.getCustomerID())) {
                            List<String> orderInfo = new ArrayList<String>();
                            String orderTime = order.getRequestedDateTime();
                            String[] parts = orderTime.split(Pattern.quote("T"));
                            orderTime = parts[1];
                            String orderDate = parts[0];
                            String orderHour = orderTime.split(Pattern.quote(":"))[0];
                            String orderMinute = orderTime.split(Pattern.quote(":"))[1];
                            String orderYear = orderDate.split(Pattern.quote("-"))[0];
                            String orderMonth = orderDate.split(Pattern.quote("-"))[1];
                            String orderDay = orderDate.split(Pattern.quote("-"))[2];
                            String amPm = (Integer.parseInt(orderHour) < 12 ? "am" : "pm");
                            int pizzasCount = 0;
                            int drinksCount = 0;
                            int sidesCount = 0;
                            for (OrderItem orderItem : cachedOrderItems) {
                                if (orderItem.getId().equals(order.getOrderID())) {
                                    boolean found = false;
                                    for (Pizza pizza : cachedPizzas) {
                                        if (orderItem.getItemName().contains(pizza.getName()) || orderItem.getItemName().contains("^")) {
                                            pizzasCount+= Integer.valueOf(orderItem.getQuantity());
                                            found = true;
                                            break;
                                        }
                                    }
                                    if (!found) {
                                        for (Drink drink : cachedDrinks) {
                                            if (orderItem.getItemName().contains(drink.getName())) {
                                                drinksCount+= Integer.valueOf(orderItem.getQuantity());
                                                found = true;
                                                break;
                                            }
                                        }
                                    }
                                    if (!found) {
                                        for (Side side : cachedSides) {
                                            if (orderItem.getItemName().contains(side.getName())) {
                                                sidesCount+= Integer.valueOf(orderItem.getQuantity());
                                                break;
                                            }
                                        }
                                    }
                                }
                            }

                            orderInfo.add("Delivery for: " + customer.getFirstName() + " " + customer.getSurname()
                                    + "\nHouse name/number: " + customer.getDeliveryLineOne()
                                    + "\nPostcode: " + customer.getDeliveryPostCode()
                                    + "\nSides: " + sidesCount
                                    + "\nDrinks: " + drinksCount
                                    + "\nPizzas: " + pizzasCount);
                            String thisOrderInfo = "\nOrder ID: " + order.getOrderID() + " Deliver by: " + orderHour + ":" + orderMinute + amPm + "  " + orderDay + "/"
                                    + orderMonth + "/" + orderYear + "\n";
                            expandableListDetail.put(thisOrderInfo, orderInfo);
                        }
                    }
                }
            }
            titles = new ArrayList<String>(expandableListDetail.keySet());

            adapter = new CustomExpandableListAdapter(this, titles, expandableListDetail, staff);
            lstOrders.setAdapter(adapter);
            if (selected > 0 && selected < titles.size())
                lstOrders.expandGroup(selected);
            viewsToRemove.clear();
            if (lstOrders != null) {
                lstOrders.setSelection(position);
                lstOrders.setSelection(position);
            }
        }

    }

    @Override
    public void onPause()
    {
        super.onPause();

        if (timerTask != null)
        {
            timerTaskRunning = false;
            timerTask.cancel(true);
            timerTask        = null;
        }
        Log.i("Thread Demo", "Activity paused");
    }


    private class TimerTask extends AsyncTask<Void, Void, Void>
    {

        //******************************************************************//
        // Executed in a separate thread in the background					//
        // Weird syntax for "some parameters may follow, but I don't know   //
        // anything about them".... 										//
        //******************************************************************//
        @Override
        protected Void doInBackground(Void... params)
        {
            while (timerTaskRunning)
            {
                try {
                    System.out.println("polling API for cache data...");
                    cachedOrders = ((OrderController) Session.get().getController("OrderController")).get(true);
                    cachedCustomers = ((CustomerController) Session.get().getController("CustomerController")).get();
                    cachedOrderItems = ((OrderedItemController) Session.get().getController("OrderedItemController")).get();
                    cachedPizzas = ((PizzaController)Session.get().getController("PizzaController")).get();
                    cachedDrinks = ((DrinksController)Session.get().getController("DrinksController")).get();
                    cachedSides = ((SideController)Session.get().getController("SideController")).get();
                    for (Order order : cachedOrders) {
                        if ((order.getStatus().equals("Ready For Delivery") || order.getStatus().equals("Out For Delivery"))) {
                            for (Customer cust : cachedCustomers) {
                                if (cust.getCustomerID().equals(order.getCustomerID())) {
                                    if (cust.getFirstName().contains("=")) {
                                        cust = (Customer)Encryption.decrypt(cust);
                                        System.out.println("Decrypting customer " + cust.getCustomerID());
                                    } else {
                                        //System.out.println("Skipping customer " + cust.getCustomerID() + " ...presum\ed already decrypted");
                                    }
                                } else if (order.getStatus().equals("Delivered")) {
                                    if (adapter != null) {
                                        for (int i = 0; i < adapter.getGroupCount(); i++) {
                                            if (order.getOrderID().equals(adapter.getGroup(i).toString().split(Pattern.quote(" "))[2])) {
                                                viewsToRemove.add(i);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    publishProgress(null);  //Invokes onProgressUpdate()
                    System.out.println("Waiting for adapter to post update status...");
                    for (int i = 0; i < 500; i++) {
                        Thread.sleep(1);
                        if (adapter != null && adapter.plsUpdate || updateRequired) {
                            adapter.plsUpdate = false;
                            updateRequired = false;
                            break;
                        }
                    }

                }
                catch (InterruptedException e)
                {
                    Log.i("Thread demo", "Timer Thread interrupted");
                }
            }
            return null;
        }


        //******************************************************************//
        // Invoked when we execute publishProgress().						//
        //******************************************************************//
        @Override
        protected void onProgressUpdate(Void... progress) {
            displayOrders();
        }
    } //End of AsyncTask
}