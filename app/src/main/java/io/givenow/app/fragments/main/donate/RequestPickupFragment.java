package io.givenow.app.fragments.main.donate;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.parse.ParseInstallation;
import com.parse.ParseUser;

import java.util.Collection;

import butterknife.Bind;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.givenow.app.R;
import io.givenow.app.adapters.DonationCategoryAdapter;
import io.givenow.app.adapters.PlaceAutocompleteAdapter;
import io.givenow.app.customviews.AdaptableGradientRectView;
import io.givenow.app.customviews.SlidingRelativeLayout;
import io.givenow.app.fragments.main.common.ConfirmRequestDialogFragment;
import io.givenow.app.fragments.main.common.MapHostingFragment;
import io.givenow.app.helpers.AttributeGetter;
import io.givenow.app.helpers.CustomAnimations;
import io.givenow.app.interfaces.AnimatorEndListener;
import io.givenow.app.models.DonationCategory;
import io.givenow.app.models.ParseUserHelper;
import io.givenow.app.models.PickupRequest;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import jp.wasabeef.recyclerview.animators.SlideInRightAnimator;
import rx.Observable;


public class RequestPickupFragment extends MapHostingFragment
        implements ResultCallback<PlaceBuffer>, AdapterView.OnItemClickListener,
        ConfirmRequestDialogFragment.ConfirmPickupDialogListener {

    private static final double AUTOCOMPLETE_BIAS_RADIUS_METERS = 10000;

    @Bind(R.id.btnBottomSubmit)
    Button btnBottomSubmit;

    @Bind(R.id.llAddressInfoContainer)
    LinearLayout llAddressInfoContainer;

    @Bind(R.id.actvAddress)
    AutoCompleteTextView actvAddress;

    @Bind(R.id.btnClearAddress)
    ImageButton btnClearAddress;

    @Bind(R.id.agrv)
    AdaptableGradientRectView adaptableGradientRectView;

    @Bind(R.id.rvDonationCategories)
    RecyclerView rvDonationCategories;

    @Bind(R.id.slidingRLContainer)
    SlidingRelativeLayout slidingRLContainer;

    @Bind(R.id.rlCurrentRequestContainer)
    SlidingRelativeLayout rlCurrentRequestContainer;

    @Bind(R.id.rvCurrentRequestCategories)
    RecyclerView rvCurrentRequestCategories;

    @Bind(R.id.tsInfo)
    TextSwitcher tsInfo;

    @Bind(R.id.ivNote)
    ImageView ivNote;

    @Bind(R.id.ivNoteSubmit)
    ImageView ivNoteSubmit;

    @Bind(R.id.llNote)
    LinearLayout llNote;

    @Bind(R.id.etNote)
    EditText etNote;

    @BindDimen(R.dimen.bottom_container_height)
    int bottomContainerHeight;

    @BindDimen(R.dimen.icon_size)
    int iconSize;

    private PlaceAutocompleteAdapter mAdapter;
    private boolean mConfirmAddressShowing = false;
    private boolean mKeyCodeBackEventHandled = false;
    private DonationCategoryAdapter mDonationCategoryAdapter;
    private boolean mCategoryLayoutShowing = false;
    private GridLayoutManager mGridLayoutManager;
    private PickupRequest mPickupRequest;
    private DonationCategoryAdapter mCurrentRequestCategoriesAdapter;
    private boolean mCurrentRequestLayoutShowing = false;

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

    @OnClick(R.id.btnClearAddress)
    public void onClearAddress(ImageButton imageButton) {
        actvAddress.setText("", false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View viewRoot = inflater.inflate(R.layout.fragment_request_pickup, container, false);
        ButterKnife.bind(this, viewRoot);

        btnClearAddress.setVisibility(View.INVISIBLE);
        // Register a listener that receives callbacks when a suggestion has been selected
        actvAddress.setOnItemClickListener(this);
        //Clear address button is only visible if address field has focus.
        actvAddress.setOnFocusChangeListener((view, hasFocus) -> {
            if (btnClearAddress != null) {
                btnClearAddress.setVisibility(hasFocus ? View.VISIBLE : View.INVISIBLE);
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
                        hideConfirmAddress().subscribe(s ->
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
                getAddressFromMapTarget().subscribe(address -> {
                    Log.i(logTag(), "Map OnCameraChanged: Setting address in input field: " + address.getAddressLine(0));
                    setAddressFieldText(address.getAddressLine(0));
//                    mListener.updateAddress(address);
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

        PickupRequest.getMyRequests().getFirstInBackground((pickupRequest, e) -> {
            if (pickupRequest != null) {
                mPickupRequest = pickupRequest;
                showCurrentRequestLayout().subscribe();
            }
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
        btnBottomSubmit.setEnabled(false);
        if (!mConfirmAddressShowing) {
            showConfirmAddress().subscribe();
        } else {
            if (!mCategoryLayoutShowing) {
                showCategoryLayout().subscribe();
            } else {
                confirmPickupRequest();
                return; //leave button disabled
            }
        }
        btnBottomSubmit.setEnabled(true);
    }

    private void confirmPickupRequest() {
        Collection<DonationCategory> items = mDonationCategoryAdapter.getSelectedItems();
        if (items.size() < 1) {
            tsInfo.setText(getString(R.string.error_insufficient_categories_selected));
            btnBottomSubmit.setEnabled(true);
        } else {
            ParseUser currUser = ParseUser.getCurrentUser();
            String myPhoneNumber = currUser.getString("phoneNumber");
            if (myPhoneNumber == null) {
                //user hasn't entered their phone before
                showConfirmPickupDialog("", "");
            } else {
                //they have entered their phone before, let's pre-populate it and their name
                String myName = currUser.getString("name");
                onFinishConfirmPickupDialog(myName, myPhoneNumber);
            }

        }
    }

    private void showConfirmPickupDialog(String name, String phoneNumber) {
        FragmentManager fm = getChildFragmentManager();
        ConfirmRequestDialogFragment confirmRequestDialogFragment =
                ConfirmRequestDialogFragment.newInstance(getString(R.string.confirm_pickup_dialog_title), name, phoneNumber,
                        getResources().getText(R.string.donor_dialog_disclaimer));
        confirmRequestDialogFragment.setOnDismissListener(dialog ->
                btnBottomSubmit.setEnabled(true));
        confirmRequestDialogFragment.show(fm, "fragment_confirm_request_dialog");
    }

    // after donor enters name and number and hits Confirm
    @Override
    public void onFinishConfirmPickupDialog(String name, String phoneNumber) {
        hideKeyboardFrom(getActivity(), getView());
        //update the current user's name and phone
        ParseUserHelper.setName(name);
        ParseUserHelper.setPhoneNumber(phoneNumber);

        // Associate the device with a user //TODO: maybe don't do this every time, only at the first time
        ParseInstallation installation = ParseInstallation.getCurrentInstallation();
        installation.put("user", ParseUser.getCurrentUser());
        installation.saveInBackground();

        //get selected categories
        Collection<DonationCategory> selectedItems = mDonationCategoryAdapter.getSelectedItems();
        //ship it off to parse
        LatLng target = getMapTarget();
        mPickupRequest = new PickupRequest(
                new ParseGeoPoint(target.latitude, target.longitude),
                name,
                actvAddress.getText().toString(),
                etNote.getText().toString(),
                phoneNumber,
                ParseUser.getCurrentUser(),
                selectedItems
        );
        savePickupRequest();
    }

    private void savePickupRequest() {
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
            new AlertDialog.Builder(getActivity())
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
        hideCategoryLayout().subscribe(v -> {
            hideConfirmAddress().subscribe();
            showCurrentRequestLayout().subscribe();
        });
    }

//    public void showConfirmAddressNow() {
//        showCurrentRequestLayout().subscribe();
//    }

    public Observable<Void> showConfirmAddress() {
        return Observable.create(subscriber -> {
            mConfirmAddressShowing = true;

            tsInfo.setText(getString(R.string.request_pickup_info_confirm_address));
            btnBottomSubmit.setText(getString(R.string.continue_label));

            // zoom in map
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(18.0f), 1000, null);

            Animator fadeInGradient = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_in);
            fadeInGradient.setTarget(adaptableGradientRectView);

            Animator growNote = CustomAnimations.animateWidth(ivNote, 0, iconSize);
            growNote.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            growNote.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    Animation shake = AnimationUtils.loadAnimation(getActivity(), R.anim.shake);
                    ivNote.startAnimation(shake);
                }
            });

            AnimatorSet set = new AnimatorSet();
            set.addListener(new AnimatorEndListener() {
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


    private Observable<Void> hideConfirmAddress() {
        return Observable.create(subscriber -> {
//            tsInfo.setText(getString(R.string.request_pickup_choose_location));
            btnBottomSubmit.setText(getString(R.string.button_set_pickup_location_label));
            Animator shrinkNote = CustomAnimations.animateWidth(ivNote, iconSize, 0);
            shrinkNote.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
            Animator fadeOutGradient = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_out);
            fadeOutGradient.setTarget(adaptableGradientRectView);

            AnimatorSet set = new AnimatorSet();
            set.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mConfirmAddressShowing = false;
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            });
            set.play(fadeOutGradient).with(shrinkNote);
            set.start();
        });
    }


    private Observable<Void> showCategoryLayout() {
        return Observable.create(subscriber -> {
            mCategoryLayoutShowing = true;
            mDonationCategoryAdapter.clearItems();
            tsInfo.setText(getString(R.string.request_pickup_info_select_categories));
            actvAddress.setEnabled(false);
            btnBottomSubmit.setText(getString(R.string.button_confirm_donation_label));
            slidingRLContainer.setVisibility(View.VISIBLE);
            Animator slideDownFromTop = AnimatorInflater.loadAnimator(getActivity(), R.animator.slide_down_from_top);
            slideDownFromTop.setInterpolator(new DecelerateInterpolator());
            slideDownFromTop.setTarget(slidingRLContainer);
            slideDownFromTop.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    buildCategoryGrid();
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            });
            slideDownFromTop.start();
        });
    }

    private void buildCategoryGrid() {
        //get categories from parse
        mDonationCategoryAdapter.clearItems();
        DonationCategory.getTop9().findInBackground((categoryList, error) -> {
            if (error == null) {
                for (DonationCategory category : categoryList) {
                    mDonationCategoryAdapter.addItem(category);
                }
            } else {
                Log.d("RPDF", "Error fetching categories: " + error.getMessage());
            }
        });
    }

    private Observable<Void> hideCategoryLayout() {
        return Observable.create(subscriber -> {
//            tsInfo.setText(getString(R.string.request_pickup_info_confirm_address));
            actvAddress.setEnabled(true);
            btnBottomSubmit.setText(getString(R.string.continue_label));

            Animator slideUpToTop = AnimatorInflater.loadAnimator(getActivity(), R.animator.slide_up_to_top);
            slideUpToTop.setInterpolator(new AccelerateInterpolator());
            slideUpToTop.setTarget(slidingRLContainer);
            slideUpToTop.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    slidingRLContainer.setVisibility(View.INVISIBLE);
                    mCategoryLayoutShowing = false;
                    subscriber.onNext(null);
                    subscriber.onCompleted();
                }
            });
            slideUpToTop.start();
        });
    }


    private Observable<Void> showCurrentRequestLayout() {
        return Observable.create(subscriber -> {
            Log.e(logTag(), "showCurrentREquestLayout");
            mCurrentRequestLayoutShowing = true;

            Collection<DonationCategory> items = mPickupRequest.getDonationCategories();
            mCurrentRequestCategoriesAdapter.clearItems();

//            mCurrentRequestCategoriesAdapter.setItems(items); //TODO might need to use List to preserve order

            tsInfo.setText(getString(R.string.request_status_waiting));
            Animator slideUp = CustomAnimations.animateHeight(rlCurrentRequestContainer, 0, bottomContainerHeight);
            slideUp.setInterpolator(new DecelerateInterpolator());
            Animator slideDown = CustomAnimations.animateHeight(btnBottomSubmit, AttributeGetter.getDimensionAttr(getActivity(), R.attr.actionBarSize), 0);
            slideDown.setInterpolator(new AccelerateInterpolator());

            Animator fade_in = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_in);
            fade_in.setTarget(adaptableGradientRectView);
            adaptableGradientRectView.setAlpha(0);
            adaptableGradientRectView.setGradientColorTo(getResources().getColor(R.color.colorPrimaryDark));

            //Disable map
            mGoogleMap.getUiSettings().setAllGesturesEnabled(false);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);

            //move & zoom map to location of current pickup request
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(mPickupRequest.getPosition(), 18);
            mGoogleMap.animateCamera(cameraUpdate);

            AnimatorSet set = new AnimatorSet();
            set.play(slideDown).before(slideUp).with(fade_in);
            set.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    btnBottomSubmit.setVisibility(View.GONE);
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
        new AlertDialog.Builder(getActivity())
                .setMessage(R.string.dialog_cancelDonation_message)
                .setPositiveButton(R.string.dialog_cancelDonation_positiveButton, (dialog, which) -> {
                    mPickupRequest.cancel();
                    mPickupRequest.saveInBackground(e -> {
                        if (e == null) {
                            hideCurrentRequestLayout().subscribe();
                        } else {
                            new AlertDialog.Builder(getActivity())
                                    .setMessage(R.string.error_donation_not_canceled)
                                    .setIcon(android.R.attr.alertDialogIcon)
                                    .show();
                        }
                    });
                })
                .setNegativeButton(R.string.dialog_cancelDonation_negativeButton, null)
                .show();
    }

    private Observable<Void> hideCurrentRequestLayout() {
        return Observable.create(subscriber -> {
            //Re-enable map
            mGoogleMap.getUiSettings().setAllGesturesEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);

            Animator slideDown = CustomAnimations.animateHeight(rlCurrentRequestContainer, bottomContainerHeight, 0);
            slideDown.setInterpolator(new AccelerateInterpolator());
            Animator slideUp = CustomAnimations.animateHeight(btnBottomSubmit, 0, AttributeGetter.getDimensionAttr(getActivity(), R.attr.actionBarSize));
            slideUp.setInterpolator(new DecelerateInterpolator());
            Animator fade_out = AnimatorInflater.loadAnimator(getActivity(), R.animator.fade_out);
            fade_out.setTarget(adaptableGradientRectView);

            AnimatorSet set = new AnimatorSet();
            set.play(fade_out).with(slideDown).before(slideUp);
            set.addListener(new AnimatorEndListener() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    adaptableGradientRectView.setGradientColorTo(getResources().getColor(R.color.colorPrimaryLight));
                    rlCurrentRequestContainer.setVisibility(View.GONE);
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

        if (mCurrentRequestLayoutShowing) {
            info = R.string.help_request_pickup_current_request;
        } else if (mCategoryLayoutShowing) {
            info = R.string.help_request_pickup_category_chooser;
        } else if (mConfirmAddressShowing) {
            info = R.string.help_request_pickup_confirm_address;
        }
        new AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.ic_help_outline_black_24dp)
                .setTitle(R.string.help_title)
                .setMessage(info)
                .show();
    }
}
