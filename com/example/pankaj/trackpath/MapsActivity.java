package com.example.pankaj.trackpath;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pankaj.trackpath.storage.Coordinates;
import com.example.pankaj.trackpath.storage.DatabaseHandler;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

import static com.example.pankaj.trackpath.LocationService.listCoord;
import static com.example.pankaj.trackpath.LocationService.mCurrentLocation;
import static com.example.pankaj.trackpath.R.id.map;
import static com.example.pankaj.trackpath.R.id.mylocation;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,  GoogleMap.OnMarkerDragListener {
    private boolean istracking = false;
    private GoogleMap mapFragment;
    private String startMarkerTime ="";
    private FloatingActionButton mylocationFloatButton, startstopFloatButton, clearFloatButton;
    private Coordinates coordObject;
    private Type type = new TypeToken<ArrayList<LatLng>>() {}.getType();
    private AlertDialog.Builder saveDBAlert;
    private Gson convertLatLngArray = new Gson();
    private DrawerLayout drawer;
    private ListView navList;
    private DatabaseHandler populatedrawer;
    private ListviewAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        saveDBAlert = new AlertDialog.Builder(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(map);
        mapFragment.getMapAsync(this);
        mapFragment.setRetainInstance(true);
        populatedrawer = new DatabaseHandler(this);
        coordObject = new Coordinates("Time","");
        adapter = new ListviewAdapter(this, populatedrawer.getAllCoordinates());
        drawer = (DrawerLayout)findViewById(R.id.drawer_layout);
        navList = (ListView) findViewById(R.id.drawer);
        navList.setAdapter(adapter);
        drawer.closeDrawer(navList);
        }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mapFragment = googleMap;
        mapFragment.getUiSettings().setZoomControlsEnabled(true);
        mapFragment.setMyLocationEnabled(true);
        mapFragment.setOnMarkerDragListener(this);
        mylocationFloatButton = (FloatingActionButton) findViewById(mylocation);
        mylocationFloatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Marking your location", Snackbar.LENGTH_SHORT).show();
                startService(new Intent(MapsActivity.this, LocationService.class));
                getMyLocation();
            }
        });

        clearFloatButton = (FloatingActionButton) findViewById(R.id.clear);
        clearFloatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Snackbar.make(view, "Clearing Map, Set your Location to Start Over!", Snackbar.LENGTH_LONG).show();
                mapFragment.clear();
//                stopService(new Intent(MapsActivity.this, LocationService.class));
//                clearFloatButton.setVisibility(View.GONE);
            }
        });

        startstopFloatButton = (FloatingActionButton) findViewById(R.id.startstopbutton);
        startstopFloatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if(istracking){
                    mapFragment.addMarker(new MarkerOptions().position(listCoord.get(listCoord.size() - 1)).title("End").flat(true));
                    drawMarker();
                    coordObject.setRawdata(convertLatLngArray.toJson(listCoord));


                    istracking = false;
                    startstopFloatButton.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_play));
                    stopService(new Intent(MapsActivity.this, LocationService.class));
//                    startstopFloatButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_light)));

                }else{
                    clearFloatButton.setVisibility(View.GONE);
                    startService(new Intent(MapsActivity.this, LocationService.class));
                    Snackbar.make(view, "Tracking Started", Snackbar.LENGTH_SHORT).show();
                    if(getMyLocation()){
                        istracking = true;
                        startstopFloatButton.setImageDrawable(getResources().getDrawable(android.R.drawable.ic_media_pause));
//                    startstopFloatButton.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_light)));
                    }


                }

            }
        });

    }

    public void drawMarker() {
        if (mapFragment != null) {
            ArrayList<LatLng> latlong = convertLatLngArray.fromJson(convertLatLngArray.toJson(listCoord), type);
            Polyline line = mapFragment.addPolyline(new PolylineOptions()
                    .addAll(latlong)
                    .width(7)
                    .color(Color.BLUE));
            mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(listCoord.get(0), 25));

        }
    }

    public void drawMarkerdb(String c) {
        if (mapFragment != null && c!="") {
            ArrayList<LatLng> latlong = convertLatLngArray.fromJson(c, type);
            mapFragment.addMarker(new MarkerOptions().position(latlong.get(0)).title("Start").flat(true));
            mapFragment.addMarker(new MarkerOptions().position(latlong.get(latlong.size()-1)).title("End").flat(true));
            Polyline line = mapFragment.addPolyline(new PolylineOptions()
                    .addAll(latlong)
                    .width(5)
                    .color(Color.RED));
            mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong.get(0), 20));

        }
    }
    protected boolean getMyLocation() {
        if (mCurrentLocation != null) {
            LatLng latlong = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
            long atTime = mCurrentLocation.getTime();
            startMarkerTime = DateFormat.getTimeInstance().format(new Date(atTime));
            coordObject.setDate(startMarkerTime);
//            coordObject.setStart(latlong);
            mapFragment.addMarker(new MarkerOptions().position(latlong).title("MyLocation ("+startMarkerTime+")").draggable(true).flat(true));
            mapFragment.animateCamera(CameraUpdateFactory.newLatLngZoom(latlong, 25));
            return true;
        }else{
            Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        listCoord.clear();
        listCoord = new ArrayList<>();
        listCoord.add(new LatLng(marker.getPosition().latitude, marker.getPosition().longitude));
        Toast.makeText(this, "Marker Droped @ Lat:" +marker.getPosition().latitude+" Long:"+ marker.getPosition().longitude, Toast.LENGTH_LONG).show();
    }

    public class ListviewAdapter extends ArrayAdapter<Coordinates> {
        Context context;
        LayoutInflater inflater;
        ArrayList<Coordinates> coordinates;
        Coordinates coordinate = new Coordinates();
        public ListviewAdapter(Context context, ArrayList<Coordinates> _coordinates){
            super(context,0,_coordinates);
            this.coordinates = _coordinates;
            this.context = context;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
        }

        public class ViewHolderItem
        {
            TextView title, delete;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ListviewAdapter.ViewHolderItem viewHolder;
            coordinate = coordinates.get(position);
            if(convertView == null){
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.listviewadapter, parent, false);
                viewHolder = new ListviewAdapter.ViewHolderItem();
                viewHolder.title = (TextView) convertView.findViewById(R.id.title);
                viewHolder.delete = (TextView) convertView.findViewById(R.id.delete);
                convertView.setTag(viewHolder);

            }
            else{
                viewHolder = (ListviewAdapter.ViewHolderItem) convertView.getTag();
            }

            if (coordinate.getDate()!=null){
                viewHolder.title.setText(coordinate.getId()+".) "+coordinate.getDate());
                viewHolder.title.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        drawMarkerdb(coordinates.get(position).getRawdata());
                        drawer.closeDrawer(navList);
                    }
                });
                viewHolder.delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        DatabaseHandler db = new DatabaseHandler(context);
                        db.deleteCoordinate(coordinates.get(position));
                        coordinates.remove(position);
                        notifyDataSetChanged();
                        Toast.makeText(context, "Deleted!", Toast.LENGTH_LONG).show();
                    }
                });

            }


            return convertView;
        }


    }
}