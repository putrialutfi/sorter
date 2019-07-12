package com.sorter.putrialutfi.sorter;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sorter.putrialutfi.sorter.Adapter.RecyclerViewAdapter;
import com.sorter.putrialutfi.sorter.Model.Things;
import com.sorter.putrialutfi.sorter.Session.SharedPrefs;

import java.util.ArrayList;
import java.util.Collections;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener,
        SearchView.OnQueryTextListener, SwipeRefreshLayout.OnRefreshListener {

    private static final String TAG_LOG = "reports main";
    private ArrayList<Things> listThings;
    private ArrayList<Things> searchResult;

    RecyclerView mRecyclerView;
    RecyclerView.Adapter mAdapter;
    RecyclerViewAdapter mRecyclerViewAdapter;
    RecyclerView.LayoutManager mLayoutManager;
    SharedPrefs mSharedPreferences;
    GoogleApiClient mGoogleApiClient;
    FirebaseAuth mFireBaseAuth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;

    ProgressDialog progressDialog;
    SearchView searchView;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        FloatingActionButton fab1 = findViewById(R.id.fab1);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addBarang();
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchView.setIconified(false);
                searchView.requestFocus();
            }
        });

        mSharedPreferences = SharedPrefs.getInstance();
        mFireBaseAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference();

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mRecyclerView = findViewById(R.id.data_list);
        mRecyclerViewAdapter = new RecyclerViewAdapter(listThings, MainActivity.this, MainActivity.this);
        swipeRefreshLayout   = findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        getSupportActionBar().setTitle("List Barang");
        recyclerView();
        getData();
    }


    @Override
    protected void onStart() {
        super.onStart();
        OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (opr.isDone()) {
            GoogleSignInResult result = opr.get();
            handleSignInResult(result);
        } else {
            opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                @Override
                public void onResult(@NonNull GoogleSignInResult googleSignInResult) {
                    handleSignInResult(googleSignInResult);
                }
            });
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        if (result.isSuccess()) {
            GoogleSignInAccount account = result.getSignInAccount();
            Log.d(TAG_LOG, "masuk sebagai " + account.getDisplayName());
        } else {
            goToLoginActivity();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        MenuItem logout = menu.findItem(R.id.action_logout);

        Intent intent = getIntent();
        String username = intent.getStringExtra("username");
        logout.setTitle("Logout ("+username+")");

        searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setIconified(true);
        searchView.setOnQueryTextListener(this);
        searchView.setFocusable(true);
        searchView.requestFocusFromTouch();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            new AlertDialog.Builder(this)
                    .setMessage("Keluar Akun?")
                    .setCancelable(false)
                    .setPositiveButton("Iya", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FirebaseAuth.getInstance().signOut();
                            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                                    new ResultCallback<Status>() {
                                        @Override
                                        public void onResult(Status status) {
                                            if (status.isSuccess()) {
                                                goToLoginActivity();
                                            } else {
                                                Log.d(TAG_LOG, "Session not close");
                                            }
                                        }
                                    });
                        }
                    })
                    .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }).create().show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void getData() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sedang Memuat Data");
        progressDialog.show();
        mDatabaseReference.child("user_data")
                .child(mFireBaseAuth.getUid())
                .child("barang")
                .orderByChild("is_deleted")
                .equalTo(0)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        listThings = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Things things = snapshot.getValue(Things.class);
                            things.setKey(snapshot.getKey());
                            listThings.add(things);
                        }
                        Collections.sort(listThings);
                        mAdapter = new RecyclerViewAdapter(listThings, MainActivity.this, MainActivity.this);
                        mRecyclerView.setAdapter(mAdapter);
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Log.d(TAG_LOG, "Data gagal dimuat karena : "+ databaseError.getMessage() + " & " + databaseError.getDetails());
                    }
                });
    }

    private void recyclerView() {
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setHasFixedSize(true);
    }

    private void addBarang() {
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_form, null);
        final EditText inputBarang = view.findViewById(R.id.input_barang);
        final EditText inputHargaEcer = view.findViewById(R.id.input_harga_ecer);
        final EditText inputHargaResell = view.findViewById(R.id.input_harga_resell);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.myDialog));
        alertDialog.setTitle("Tambah Item");
        alertDialog.setView(LayoutInflater.from(this).inflate(R.layout.dialog_form, null));
        alertDialog.setView(view);

        alertDialog.setPositiveButton("Tambah", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String getUserID = mFireBaseAuth.getCurrentUser().getUid();
                String barang = inputBarang.getText().toString();
                String hargaEcer = inputHargaEcer.getText().toString();
                String hargaSeller = inputHargaResell.getText().toString();
                int isdeleted = 0;

                if (barang.isEmpty() || hargaEcer.isEmpty() || hargaSeller.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Isian Tidak Boleh Kosong", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d(TAG_LOG, "masukkan data");
                    mDatabaseReference.child("user_data")
                            .child(getUserID)
                            .child("barang").push().setValue(new Things(barang, hargaEcer, hargaSeller, isdeleted))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Data berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG_LOG, "Gagal ditambahkan karena " + e.getCause());
                                }

                            });
                }
            }
        });
        alertDialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.create();
        alertDialog.show();
    }

    public void updateBarang(int position){
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_form, null);
        final EditText inputBarang = view.findViewById(R.id.input_barang);
        final EditText inputHargaEcer = view.findViewById(R.id.input_harga_ecer);
        final EditText inputHargaResell = view.findViewById(R.id.input_harga_resell);

        inputBarang.setText(listThings.get(position).getNama_barang());
        inputHargaEcer.setText(listThings.get(position).getHarga_ecer());
        inputHargaResell.setText(listThings.get(position).getHarga_resell());
        final String primaryKey = listThings.get(position).getKey();

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.this, R.style.myDialog));
        alertDialog.setTitle("Update Item");
        alertDialog.setView(LayoutInflater.from(this).inflate(R.layout.dialog_form, null));
        alertDialog.setView(view);

        alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String getUserID = mFireBaseAuth.getCurrentUser().getUid();
                String barang = inputBarang.getText().toString();
                String hargaEcer = inputHargaEcer.getText().toString();
                String hargaSeller = inputHargaResell.getText().toString();
                int isdeleted = 0;

                if (barang.isEmpty() || hargaEcer.isEmpty() || hargaSeller.isEmpty()) {
                    Toast.makeText(getApplicationContext(), "Isian Tidak Boleh Kosong", Toast.LENGTH_SHORT).show();
                }
                else {
                    Log.d(TAG_LOG, "update data");
                    mDatabaseReference.child("user_data")
                            .child(getUserID)
                            .child("barang")
                            .child(primaryKey).setValue(new Things(barang, hargaEcer, hargaSeller, isdeleted))
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getApplicationContext(), "Data berhasil diupdate", Toast.LENGTH_SHORT).show();
                                    inputBarang.setText("");
                                    inputHargaEcer.setText("");
                                    inputHargaResell.setText("");
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.d(TAG_LOG, "Gagal memperbarui data karena " + e.getCause());
                                }

                            });
                }
            }
        });
        alertDialog.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialog.create();
        alertDialog.show();
    }

    public void deleteBarang(int position) {
        //actually this is updating deleted status on db
        final String barang = listThings.get(position).getNama_barang();
        final String hargaEcer = listThings.get(position).getHarga_ecer();
        final String hargaSeller = listThings.get(position).getHarga_resell();
        final int isdeleted = 1;

        final String getUserID = mFireBaseAuth.getCurrentUser().getUid();
        final String dataKey = listThings.get(position).getKey();
        new AlertDialog.Builder(this)
                .setMessage("Hapus Data?")
                .setCancelable(false)
                .setPositiveButton("Iya", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(mDatabaseReference != null) {
                            mDatabaseReference.child("user_data")
                                    .child(getUserID)
                                    .child("barang")
                                    .child(dataKey).setValue(new Things(barang, hargaEcer, hargaSeller, isdeleted))
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getApplicationContext(), "Data berhasil dihapus", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d(TAG_LOG, "Gagal memperbarui data karena " + e.getCause());
                                        }

                                    });
                        }
                    }
                })
                .setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create().show();
    }

    private void goToLoginActivity() {
        mSharedPreferences.isLogedIn(this, false);
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG_LOG, "Connection Failed");
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        String textQuery =  query.toLowerCase();
        searchData(textQuery);
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        String textQuery =  newText.toLowerCase();
        searchData(textQuery);
        return false;
    }

    private void searchData(final String keyword) {
        final ArrayList<Things> resultsThings = new ArrayList<>();
        if (searchResult == null) {
            searchResult = listThings;
        }
        if (keyword != null){
            if (listThings != null & searchResult.size()>0) {
                for (final Things things : searchResult) {
                    if (things.getNama_barang().toLowerCase().contains(keyword.toString())) resultsThings.add(things);
                }
            }
            Collections.sort(resultsThings);
            mAdapter = new RecyclerViewAdapter(resultsThings,MainActivity.this, MainActivity.this);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        getData();
        mRecyclerViewAdapter.notifyDataSetChanged();
        swipeRefreshLayout.setRefreshing(false);
    }
}
