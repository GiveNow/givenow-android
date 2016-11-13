package io.givenow.app.fragments.main.donate;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextSwitcher;

import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

import java.util.Collection;

import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import io.givenow.app.GiveNowApplication;
import io.givenow.app.R;
import io.givenow.app.adapters.DonationCategoryAdapter;
import io.givenow.app.adapters.PlaceAutocompleteAdapter;
import io.givenow.app.customviews.AdaptableGradientRectView;
import io.givenow.app.customviews.SlidingRelativeLayout;
import io.givenow.app.fragments.PhoneNumberVerificationFragment;
import io.givenow.app.fragments.PhoneNumberVerificationFragmentBuilder;
import io.givenow.app.fragments.main.common.MapHostingFragment;
import io.givenow.app.helpers.Analytics;
import io.givenow.app.helpers.CustomAnimations;
import io.givenow.app.helpers.ErrorDialogs;
import io.givenow.app.helpers.RateApp;
import io.givenow.app.helpers.ResourceHelper;
import io.givenow.app.helpers.ViewHelper;
import io.givenow.app.models.DonationCategory;
import io.givenow.app.models.ParseUserHelper;
import io.givenow.app.models.PickupRequest;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import rx.Observable;
import rx.parse.ParseObservable;

import static rx.android.schedulers.AndroidSchedulers.mainThread;


public class RequestPickupFragment extends MapHostingFragment
        implements ResultCallback<PlaceBuffer>,
        AdapterView.OnItemClickListener,
        PhoneNumberVerificationFragment.OnUserLoginCompleteListener,
        DialogInterface.OnDismissListener {

    private static final double AUTOCOMPLETE_BIAS_RADIUS_METERS = 10000;

    @BindView(R.id.btnBottomSubmit)
    Button btnBottomSubmit;

    @BindView(R.id.llAddressInfoContainer)
    LinearLayout llAddressInfoContainer;

    @BindView(R.id.actvAddress)
    AutoCompleteTextView actvAddress;

    @BindView(R.id.btnClearAddress)
    ImageButton btnClearAddress;

    @BindView(R.id.agrv)
    AdaptableGradientRectView adaptableGradientRectView;

    @BindView(R.id.rvDonationCategories)
    RecyclerView rvDonationCategories;

    @BindView(R.id.slidingRLContainer)
    SlidingRelativeLayout slidingRLContainer;

    @BindView(R.id.rlCurrentRequestContainer)
    SlidingRelativeLayout rlCurrentRequestContainer;

    @BindView(R.id.rvCurrentRequestCategories)
    RecyclerView rvCurrentRequestCategories;

    @BindView(R.id.tsInfo)
    TextSwitcher tsInfo;

    @BindView(R.id.ivNote)
    ImageView ivNote;

    @BindView(R.id.ivNoteSubmit)
    ImageView ivNoteSubmit;

    @BindView(R.id.llNote)
    LinearLayout llNote;

    @BindView(R.id.etNote)
    EditText etNote;

    @BindDimen(R.dimen.bottom_container_height)
    int bottomContainerHeight;

    @BindDimen(R.dimen.icon_size)
    int iconSize;

    @BindView(R.id.fabMyLocation)
    FloatingActionButton fabMyLocation;

    private PlaceAutocompleteAdapter mAdapter;
    private boolean mConfirmAddressShowing = false;
    private boolean mKeyCodeBackEventHandled = false;
    private DonationCategoryAdapter mDonationCategoryAdapter;
    private boolean mCategoryLayoutShowing = false;
    private GridLayoutManager mGridLayoutManager;
    @Nullable private PickupRequest mPickupRequest;
    private DonationCategoryAdapter mCurrentRequestCategoriesAdapter;
    private boolean mCurrentRequestLayoutShowing = false;
    private Tracker mTracker;
    private Unbinder unbinder;

    public RequestPickupFragment() {
        // Required empty public constructor
    }

    public static RequestPickupFragment newInstance() {
        // strange. I can't use a constructor, I have to define this newInstance method and
        // call this in order to get a usable instance of this fragment.
        RequestPickupFragment f = new RequestPickupFragment();
        return f;
    }

    public static void hideKeyboardFrom(Context context, View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @SuppressLint("NewApi")
    @OnClick(R.id.btnClearAddress)
    public void onClearAddress(ImageButton imageButton) {
        actvAddress.setText("", false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GiveNowApplication application = (GiveNowApplication) getActivity().getApplication();
        mTracker = application.getDefaultTracker();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View viewRoot = inflater.inflate(R.layout.fragment_request_pickup, container, false);
        unbinder = ButterKnife.bind(this, viewRoot);

        btnClearAddress.setVisibility(View.INVISIBLE);
        // Register a listener that receives callbacks when a suggestion has been selected
        actvAddress.setOnItemClickListener(this);
        //Clear address button is only visible if address field has focus.
        actvAddress.setOnFocusChangeListener((view, hasFocus) -> {
            if (btnClearAddress != null) {
                btnClearAddress.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
            }
        });

        etNote.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    ivNote.setImageResource(R.drawable.ic_playlist_add_check_white_24dp);
                } else {
                    ivNote.setImageResource(R.drawable.ic_playlist_add_white_24dp);
                }
            }
        });

        //Catch back button and animate away relevant views
        viewRoot.setOnKeyListener((v, keyCode, event) -> {
            //This event is raised twice for a back button press. Not sure why, but
            // here's a hack to only handle the first event.
            if (mKeyCodeBackEventHandled) {
                Log.d(logTag(), "Ignoring extra Back event.");
                mKeyCodeBackEventHandled = false;
                return true;
            } else {
                mKeyCodeBackEventHandled = true;
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    if (mCategoryLayoutShowing) {
                        hideCategoryLayout().subscribe(s ->
                                tsInfo.setText(getString(R.string.request_pickup_info_confirm_address)));
                        return true;
                    } else if (mConfirmAddressShowing) {
                        hideConfirmAddress(true).subscribe(s ->
                                tsInfo.setText(getString(R.string.request_pickup_choose_location)));
                        return true;
                    }
                }
                return false;
            }
        });

        mDonationCategoryAdapter = new DonationCategoryAdapter();
        rvDonationCategories.setHasFixedSize(true);
        rvDonationCategories.setItemAnimator(new ScaleInAnimator(new DecelerateInterpolator()));
        rvDonationCategories.setAdapter(mDonationCategoryAdapter);
        mGridLayoutManager = new GridLayoutManager(getActivity(), 3);  // The number of Columns
        rvDonationCategories.setLayoutManager(mGridLayoutManager);

        mCurrentRequestCategoriesAdapter = new DonationCategoryAdapter();
        mCurrentRequestCategoriesAdapter.setCardWidth(getResources().getDimensionPixelSize(R.dimen.card_horizontal_column_width));
        rvCurrentRequestCategories.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        rvCurrentRequestCategories.setItemAnimator(new SlideInRightAnimator(new DecelerateInterpolator()));
        rvCurrentRequestCategories.setAdapter(mCurrentRequestCategoriesAdapter);

        tsInfo.setFactory(() -> LayoutInflater.from(getActivity()).inflate(R.layout.textview_info, null));
        Log.w(logTag(), "onCreateView completed.");
        return viewRoot;
    }

    @Override
    public void onMapReady(GoogleMap map) {
        super.onMapReady(map);
        map.getUiSettings().setCompassEnabled(false);

        map.setOnCameraChangeListener(cameraPosition -> {
            if (!isMapTouched()) {
                // save cpu cycles, only recalculate if we're not pressed, ie the user lifted their finger off
                getAddressFromMapTarget().subscribe(
                        address -> {
                            Log.i(logTag(), "Map OnCameraChanged: Setting address in input field: " + address.getAddressLine(0));
                            setAddressFieldText(address.getAddressLine(0));
                        },
                        error -> {
                            error.printStackTrace();
                            setAddressFieldText(getString(R.string.address_field_error));
                        });
            } else {
                //can remove the detail fragment here, but per uber UX we keep it displayed
            }

        });

        // Set up the adapter that will retrieve suggestions from the Places Geo Data API that cover
        // the entire world.
        mAdapter = new PlaceAutocompleteAdapter(getActivity(),
                getmGoogleApiClient(),
                null,
                null //AutocompleteFilter.create(Collections.singletonList(Place.TYPE_STREET_ADDRESS)) //Ugh, this doesnt work because google doesn't actually support the `address` filter on android.
        );
        actvAddress.setAdapter(mAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();

        fetchPickupRequestStatus();
    }

    private void fetchPickupRequestStatus() {
        ParseObservable.first(PickupRequest.Companion.queryMyRequests()).observeOn(mainThread()).subscribe(
                pickupRequest -> {
                    mPickupRequest = pickupRequest;
                    if (pickupRequest.getDonation().isNone()) {
                        if (isAdded()) { // In rare cases, we get here and we're still detached.
                            showCurrentRequestLayout().subscribe();
                        } else { // So if we're not added yet, post this as a runnable that will run when we are.
                            rlCurrentRequestContainer.post(() -> showCurrentRequestLayout().subscribe());
                        }
                    } else {
                        fj.data.List<DonationCategory> dc = fj.data.List.list(pickupRequest.getDonationCategories());
                        new AlertDialog.Builder(getActivity())
                                .setIcon(R.mipmap.ic_launcher)
                                .setTitle(R.string.donation_complete_title)
                                .setMessage(getString(R.string.donation_complete_message_head) + " " +
                                        (dc.isSingle() ?
                                                dc.head().getName(getContext())
                                                : dc.head().getName(getContext()) +
                                                dc.tail().init().foldLeft(
                                                        (s, category) -> s + ", " + category.getName(getContext()),
                                                        "") +
                                                " " + getString(R.string.and) + " " + dc.last().getName(getContext())
                                        ) +
                                        " " + getString(R.string.donation_complete_message_tail))
                                .setPositiveButton(R.string.done, null)
                                .setNeutralButton(R.string.rate_app, (d, w) -> RateApp.rateNow(getActivity()))
                                .setOnDismissListener(d -> {
                                    if (mPickupRequest != null) {
                                        mPickupRequest.markComplete()
                                                .flatMap(response -> {
                                                    Log.d("Cloud Response", response.toString());
                                                    return Observable.just(response);
                                                })
                                                .flatMap(p -> mCurrentRequestLayoutShowing ?
                                                        hideCurrentRequestLayout() : Observable.just(null)
                                                )
                                                .subscribe(
                                                        v -> mPickupRequest = null,
                                                        error -> ErrorDialogs.connectionFailure(getActivity(), error));

                                    }
                                })
                                .show();
                        //TODO: Share on facebook/twitter etc for bragging rights
                    }
                },
                error -> {
                    Log.d(logTag(), "No outstanding pickup request.");
                });
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        /* Retrieve the place ID of the selected item from the Adapter.
         The adapter stores each Place suggestion in a AutocompletePrediction from which we
         read the place ID and title.*/
        final AutocompletePrediction item = mAdapter.getItem(position);
        final String placeId = item.getPlaceId();
        final CharSequence primaryText = item.getPrimaryText(null);

        Log.i(logTag(), "Autocomplete item selected: " + primaryText);

        // Issue a request to the Places Geo Data API to retrieve a Place object with additional details about the place.
        Places.GeoDataApi
                .getPlaceById(RequestPickupFragment.this.getmGoogleApiClient(), placeId)
                .setResultCallback(RequestPickupFragment.this);

        setAddressFieldText(primaryText.toString());
        deselectAddressField();
        Log.i(logTag(), "Called getPlaceById to get Place details for " + placeId);
    }

    @SuppressLint("NewApi")
    public void setAddressFieldText(String text) {
        actvAddress.setText(text, false); // setText, and disable autocompletion.
        actvAddress.setSelection(text.length());
    }

    public void deselectAddressField() {
        actvAddress.clearFocus();
        hideKeyboardFrom(getActivity(), flMapLayout);
    }

    @OnClick(R.id.btnBottomSubmit)
    protected void onBottomSubmit(Button button) {
        String label;
        btnBottomSubmit.setEnabled(false);
        if (!mConfirmAddressShowing) {
            showConfirmAddress().subscribe();
            label = "withSetPickupLocationShowing";
        } else {
            if (!mCategoryLayoutShowing) {
                showCategoryLayout().subscribe();
                label = "withConfirmAddressShowing";
            } else {
                confirmPickupRequest();
                label = "withCategoryLayoutShowing";
                Analytics.sendHit(mTracker, "RequestPickup", "BottomButtonClicked", label);
                return; //leave button disabled
            }
        }
        Crashlytics.setString("RequestPickup.BottomButtonClicked", label);
        Analytics.sendHit(mTracker, "RequestPickup", "BottomButtonClicked", label);
        btnBottomSubmit.setEnabled(true);

    }

    private void confirmPickupRequest() {
        HitBuilders.EventBuilder hit = new HitBuilders.EventBuilder();
        hit.setCategory("RequestPickup").setAction("PickupRequestPreSave");

        Collection<DonationCategory> items = mDonationCategoryAdapter.getSelectedItems();
        if (items.size() < 1) {
            tsInfo.setText(getString(R.string.error_insufficient_categories_selected));
            btnBottomSubmit.setEnabled(true);
        } else {
            if (!ParseUserHelper.isRegistered()) {
                //user is still anonymous
                hit.setLabel("ByAnonymousUser");
                showPhoneNumberDialog();
            } else {
                hit.setLabel(ParseUser.getCurrentUser().getObjectId());
                constructPickupRequest();
                savePickupRequest();
            }
        }
        mTracker.send(hit.build());
    }

    private void showPhoneNumberDialog() {
        new PhoneNumberVerificationFragmentBuilder()
                .build()
                .show(getChildFragmentManager(), "phdialog");
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        btnBottomSubmit.setEnabled(true);
    }

    @Override
    public void onUserLoginComplete() {
        PickupRequest.Companion.queryMyRequests().getFirstInBackground((pickupRequest, e) -> {
            if (pickupRequest != null) {
                new AlertDialog.Builder(getContext())
                        .setTitle(getString(R.string.dialog_loggedin_existing_donation_found_title))
                        .setMessage(getString(R.string.dialog_loggedin_existing_donation_found_message))
                        .setPositiveButton(getString(R.string.dialog_loggedin_existing_donation_found_button), null)
                        .setOnDismissListener(dialog -> {
                            mPickupRequest = pickupRequest;
                            onPickupRequestSaved();
                        })
                        .show();
            } else {
                hideKeyboardFrom(getActivity(), getView());
                constructPickupRequest();
                savePickupRequest();
            }
        });
    }

    private void constructPickupRequest() {
        //get selected categories
        //ship it off to parse
        LatLng target = getMapTarget();
        mPickupRequest = new PickupRequest(
                new ParseGeoPoint(target.latitude, target.longitude),
                actvAddress.getText().toString(),
                etNote.getText().toString(),
                ParseUser.getCurrentUser(),
                mDonationCategoryAdapter.getSelectedItems()
        );
    }

    private void savePickupRequest() {
        Analytics.sendHit(mTracker, "RequestPickup", "PickupRequestTrySave", ParseUser.getCurrentUser().getObjectId());

        getActivity().setProgressBarIndeterminateVisibility(true);

        mPickupRequest.saveInBackground(this::shouldWeRetrySave);
    }

    public void shouldWeRetrySave(ParseException e) {
        if (e == null) {
            // saved successfully
            // detach this detail fragment, we're done here
            onPickupRequestSaved();
        } else {
            // save did not succeed
            getActivity().setProgressBarIndeterminateVisibility(false);
            // show error notification dialog with retry or cancel
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.pickupRequest_retryDialog_title)
                    .setMessage(R.string.pickupRequest_retryDialog_message)
                    .setPositiveButton(R.string.pickupRequest_retryDialog_retryLabel, (dialog, which) -> savePickupRequest())
                    .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                        // do nothing
                    })
                    .setIconAttribute(android.R.attr.alertDialogIcon)
                    .show();
        }
    }

    private void onPickupRequestSaved() {
        Analytics.sendHit(mTracker, "RequestPickup", "PickupRequestSaved", ParseUser.getCurrentUser().getObjectId());

        hideCategoryLayout().subscribe(v -> {
            hideConfirmAddress(false).subscribe();
            showCurrentRequestLayout().subscribe();
        });
    }

//    public void showConfirmAddressNow() {
//        showCurrentRequestLayout().subscribe();
//    }

    @NonNull
    public Observable<Void> showConfirmAddress() {
        return Observable.create(subscriber -> {
            mConfirmAddressShowing = true;

            tsInfo.setText(getString(R.string.request_pickup_info_confirm_address));
            btnBottomSubmit.setText(getString(R.string.continue_label));

            // zoom in map
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f), 1000, null);

            Animator fadeInGradient = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_in);
            fadeInGradient.setTarget(adaptableGradientRectView);

            Animator growNote = CustomAnimations.growWidthAndShake(ivNote, 0, iconSize);

            AnimatorSet set = new AnimatorSet();
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            });
            set.play(fadeInGradient).before(growNote);
            set.start();
        });
    }

    @OnClick(R.id.ivNote)
    public void onNoteClick(ImageView iv) {
        //show llNote by sliding it down from top (over map)
        Animator slideDownllNote = ViewPropertyObjectAnimator.animate(llNote).translationY(llAddressInfoContainer.getBottom()).setInterpolator(new DecelerateInterpolator()).get();

        //animate ivNote down to bottom of llnote
        Animator dropIvNoteDown = ViewPropertyObjectAnimator.animate(ivNote).translationY(llNote.getBottom()).setInterpolator(new AccelerateInterpolator()).get();

        //shrink ivNote
        Animator shrinkIvNote = CustomAnimations.circularHide(ivNote);

        //grow ivNoteSubmit
        Animator growIvNoteSubmit = CustomAnimations.circularReveal(ivNoteSubmit);

        //or maybe animate ivNote left to where ivNoteOpen is
        AnimatorSet set = new AnimatorSet();
        set.play(slideDownllNote).with(dropIvNoteDown);
        set.play(shrinkIvNote).before(growIvNoteSubmit).after(dropIvNoteDown); //TODO: experiemnt
        set.start();
    }

    @OnClick(R.id.ivNoteSubmit)
    public void onNoteSubmit(ImageView iv) {
        if (mPickupRequest == null) {
            hideNoteField();
        } else {
            mPickupRequest.setNote(etNote.getText().toString());
            ParseObservable.save(mPickupRequest).observeOn(mainThread()).subscribe(
                    pickupRequest -> {
                        hideNoteField();
                    },
                    error -> ErrorDialogs.connectionFailure(getContext(), error));
        }
    }


    private void hideNoteField() {
        //shrink ivNoteSubmit
        Animator shrinkIvNoteSubmit = CustomAnimations.circularHide(ivNoteSubmit);
        //grow ivNote
        Animator growIvNote = CustomAnimations.circularReveal(ivNote);
        //animate ivNote up to height of llAddress
        Animator flyIvNoteUp = ViewPropertyObjectAnimator.animate(ivNote).translationY(0).setInterpolator(new AccelerateDecelerateInterpolator()).get();

        // maybe fade out llNote
        //hide slidingRLNoteLayout by sliding it up to top (over map)
        Animator slideUpllNote = ViewPropertyObjectAnimator.animate(llNote).translationY(0).setInterpolator(new AccelerateInterpolator()).get();

        AnimatorSet set = new AnimatorSet();
        set.play(shrinkIvNoteSubmit).before(growIvNote);
        set.play(flyIvNoteUp).with(slideUpllNote).after(growIvNote);
        set.start();
    }


    @NonNull
    private Observable<Void> hideConfirmAddress(boolean shrinkNoteButton) {
        return Observable.create(subscriber -> {
//            tsInfo.setText(getString(R.string.request_pickup_choose_location));
            btnBottomSubmit.setText(getString(R.string.button_set_pickup_location_label));

            Animator fadeOutGradient = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_out);
            fadeOutGradient.setTarget(adaptableGradientRectView);

            AnimatorSet set = new AnimatorSet();
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mConfirmAddressShowing = false;
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            });

            if (shrinkNoteButton) {
                Animator shrinkNote = CustomAnimations.animateWidth(ivNote, iconSize, 0);
                shrinkNote.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
                set.play(fadeOutGradient).with(shrinkNote);
            } else {
                set.play(fadeOutGradient);
            }
            set.start();
        });
    }


    @NonNull
    private Observable<Void> showCategoryLayout() {
        return Observable.create(subscriber -> {
            mCategoryLayoutShowing = true;
            mDonationCategoryAdapter.clearItems();
            tsInfo.setText(getString(R.string.request_pickup_info_select_categories));
            actvAddress.setEnabled(false);
            btnBottomSubmit.setText(getString(R.string.button_confirm_donation_label));
            ViewHelper.safeVisible(slidingRLContainer);
            Animator slideDownFromTop = AnimatorInflater.loadAnimator(getActivity(), R.animator.slide_down_from_top);
            slideDownFromTop.setInterpolator(new DecelerateInterpolator());
            slideDownFromTop.setTarget(slidingRLContainer);
            slideDownFromTop.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            });
            buildCategoryGrid();
            slideDownFromTop.start();
        });
    }

    private void buildCategoryGrid() {
        //get categories from parse
        mDonationCategoryAdapter.clearItems();
        ParseObservable.find(DonationCategory.fetchTop9()).observeOn(mainThread()).subscribe(
                mDonationCategoryAdapter::addItem,
                error -> ErrorDialogs.connectionFailure(getActivity(), error)
        );
    }

    @NonNull
    private Observable<Void> hideCategoryLayout() {
        return Observable.create(subscriber -> {
//            tsInfo.setText(getString(R.string.request_pickup_info_confirm_address));
            btnBottomSubmit.setText(getString(R.string.continue_label));

            Animator slideUpToTop = AnimatorInflater.loadAnimator(getActivity(), R.animator.slide_up_to_top);
            slideUpToTop.setInterpolator(new AccelerateInterpolator());
            slideUpToTop.setTarget(slidingRLContainer);
            slideUpToTop.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ViewHelper.safeInvisible(slidingRLContainer);
                    actvAddress.setEnabled(true);
                    mCategoryLayoutShowing = false;
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            });
            slideUpToTop.start();
        });
    }


    @NonNull
    private Observable<Void> showCurrentRequestLayout() {
        return Observable.create(subscriber -> {
            Log.e(logTag(), "showCurrentREquestLayout");
            mCurrentRequestLayoutShowing = true;

            Collection<DonationCategory> items = mPickupRequest.getDonationCategories();
            mCurrentRequestCategoriesAdapter.clearItems();

//            mCurrentRequestCategoriesAdapter.setItems(items); //TODO might need to use List to preserve order

            if (mPickupRequest.getPendingVolunteer().isNone()) {
                tsInfo.setText(getString(R.string.request_status_waiting));
            } else if (mPickupRequest.getPendingVolunteer().isSome() && mPickupRequest.getConfirmedVolunteer().isNone()) {
                tsInfo.setText(getString(R.string.request_status_volunteer_pending));
            } else if (mPickupRequest.getPendingVolunteer().isSome() && mPickupRequest.getConfirmedVolunteer().isSome()) {
                tsInfo.setText(getString(R.string.request_status_volunteer_confirmed, ParseUserHelper.getPhoneNumber()));
            }

            Animator slideUp = CustomAnimations.animateHeight(rlCurrentRequestContainer, 0, bottomContainerHeight);
            slideUp.setInterpolator(new DecelerateInterpolator());
            Animator slideDown = CustomAnimations.animateHeight(btnBottomSubmit, ResourceHelper.getDimensionAttr(getActivity(), R.attr.actionBarSize), 0);
            slideDown.setInterpolator(new AccelerateInterpolator());

            Animator fade_in = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_in);
            fade_in.setTarget(adaptableGradientRectView);
            adaptableGradientRectView.setAlpha(0);
            adaptableGradientRectView.setGradientColorTo(getResources().getColor(R.color.colorPrimaryDark));

            Animator growNote = CustomAnimations.growWidthAndShake(ivNote, 0, iconSize);

            //Disable map and address field
            actvAddress.setEnabled(false);
            mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
            Animator hideFab = CustomAnimations.circularHide(fabMyLocation);

            //Update address and note field
            actvAddress.setText(mPickupRequest.getAddress(), false);
            etNote.setText(mPickupRequest.getNote());

            //Move & zoom map to location of current pickup request
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mPickupRequest.getPosition(), 18);
            mGoogleMap.animateCamera(cameraUpdate);

            AnimatorSet set = new AnimatorSet();
            set.play(hideFab);
            set.play(slideDown).before(slideUp).with(fade_in).with(growNote);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    ViewHelper.safeGone(btnBottomSubmit);
                    for (DonationCategory item : items) {
                        item.setSelected(true);
                        item.setClickable(false);
                        mCurrentRequestCategoriesAdapter.addItem(item);
                    }
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            });
            set.start();
        });
    }

    @OnClick(R.id.llCancelContainer)
    public void onCancelDonation(LinearLayout llCancelContainer) {
        new AlertDialog.Builder(getContext())
                .setMessage(R.string.dialog_cancelDonation_message)
                .setPositiveButton(R.string.dialog_cancelDonation_positiveButton, (dialog, which) -> {
                    mPickupRequest.cancel();
                    mPickupRequest.saveInBackground(e -> {
                        if (e == null) {
                            Analytics.sendHit(mTracker, "RequestPickup", "DonationCanceled", ParseUser.getCurrentUser().getObjectId());

                            hideCurrentRequestLayout().subscribe();
                        } else {
                            ErrorDialogs.connectionFailure(getContext(), e);
                        }
                    });
                })
                .setNegativeButton(R.string.dialog_cancelDonation_negativeButton, null)
                .show();
    }

    @NonNull
    private Observable<Void> hideCurrentRequestLayout() {
        return Observable.create(subscriber -> {
            //Re-enable map and address field
            actvAddress.setEnabled(true);
            mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
            Animator showFab = CustomAnimations.circularReveal(fabMyLocation);

            Animator slideDown = CustomAnimations.animateHeight(rlCurrentRequestContainer, bottomContainerHeight, 0);
            slideDown.setInterpolator(new AccelerateInterpolator());
            Animator slideUp = CustomAnimations.animateHeight(btnBottomSubmit, 0, ResourceHelper.getDimensionAttr(getActivity(), R.attr.actionBarSize));
            slideUp.setInterpolator(new DecelerateInterpolator());
            Animator fade_out = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_out);
            fade_out.setTarget(adaptableGradientRectView);

            AnimatorSet set = new AnimatorSet();
            set.play(fade_out).with(slideDown).before(slideUp);
            set.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    showFab.start();
                    adaptableGradientRectView.setGradientColorTo(getResources().getColor(R.color.colorPrimaryLight));
                    ViewHelper.safeGone(rlCurrentRequestContainer);
                    btnBottomSubmit.setEnabled(true);
                    tsInfo.setText(getString(R.string.request_pickup_choose_location));
                    mCurrentRequestLayoutShowing = false;
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            });
            set.start();
        });
    }

    @Override
    public void onResult(PlaceBuffer places) {
        if (!places.getStatus().isSuccess()) {
            // Request did not complete successfully
            Log.e(logTag(), "Place query did not complete. Error: " + places.getStatus().toString());
            places.release();
            return;
        }
        // Get the Place object from the buffer.
        Place place = places.get(0);

        // Animate map to place
        // perhaps maintain zoom level?
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 13);
        mGoogleMap.animateCamera(cameraUpdate);

        Log.i(logTag(), "Place details received: " + place.getName());
        places.release();
    }

    public final String logTag() {
        return this.getClass().getSimpleName();
    }

    @Override
    public void onConnected(Bundle dataBundle) {
        super.onConnected(dataBundle);
        mAdapter.setBounds(convertCenterAndRadiusToBounds(getLastLocation().orSome(getMapTarget()), AUTOCOMPLETE_BIAS_RADIUS_METERS));
    }

    public void displayInfo() {
        int info = R.string.help_request_pickup_initial;
        int ga_label = 0;


        if (mCurrentRequestLayoutShowing) {
            info = R.string.help_request_pickup_current_request;
            ga_label = 1;
        } else if (mCategoryLayoutShowing) {
            info = R.string.help_request_pickup_category_chooser;
            ga_label = 2;
        } else if (mConfirmAddressShowing) {
            info = R.string.help_request_pickup_confirm_address;
            ga_label = 3;
        }

        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Help")
                .setAction("Clicked")
                .setLabel(ParseUser.getCurrentUser().getObjectId())
                .setValue(ga_label)
                .build());

        new AlertDialog.Builder(getContext())
                .setIcon(R.drawable.ic_help_outline_black_24dp)
                .setTitle(R.string.help_title)
                .setMessage(info)
                .show();
    }

    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
