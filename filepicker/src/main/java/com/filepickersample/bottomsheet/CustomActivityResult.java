package com.filepickersample.bottomsheet;

import android.content.Intent;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCaller;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * https://stackoverflow.com/questions/62671106/onactivityresult-method-is-deprecated-what-is-the-alternative
 *
 * Uses :
 * public class BaseActivity extends AppCompatActivity {
 *     protected final BetterActivityResult<Intent, ActivityResult> activityLauncher = BetterActivityResult.registerActivityForResult(this);
 * }
 *
 * public void openSomeActivityForResult() {
 *     Intent intent = new Intent(this, SomeActivity.class);
 *     activityLauncher.launch(intent, result -> {
 *         if (result.getResultCode() == Activity.RESULT_OK) {
 *             // There are no request codes
 *             Intent data = result.getData();
 *             doSomeOperations();
 *         }
 *     })
 * }
 * */

public class CustomActivityResult<Input, Result> {
    /**
     * Register activity result using a {@link ActivityResultContract} and an in-place activity result callback like
     * the default approach. You can still customise callback using {@link #launch(Object, OnActivityResult)}.
     */
    @NonNull
    public static <Input, Result> CustomActivityResult<Input, Result> registerForActivityResult(
            @NonNull ActivityResultCaller caller,
            @NonNull ActivityResultContract<Input, Result> contract,
            @Nullable OnActivityResult<Result> onActivityResult) {
        return new CustomActivityResult<>(caller, contract, onActivityResult);
    }

    /**
     * Same as {@link #registerForActivityResult(ActivityResultCaller, ActivityResultContract, OnActivityResult)} except
     * the last argument is set to {@code null}.
     */
    @NonNull
    public static <Input, Result> CustomActivityResult<Input, Result> registerForActivityResult(
            @NonNull ActivityResultCaller caller,
            @NonNull ActivityResultContract<Input, Result> contract) {
        return registerForActivityResult(caller, contract, null);
    }

    /**
     * Specialised method for launching new activities.
     */
    @NonNull
    public static CustomActivityResult<Intent, ActivityResult> registerActivityForResult(
            @NonNull ActivityResultCaller caller) {
        return registerForActivityResult(caller, new ActivityResultContracts.StartActivityForResult());
    }

    /**
     * Callback interface
     */
    public interface OnActivityResult<O> {
        /**
         * Called after receiving a result from the target activity
         */
        void onActivityResult(O result);
    }

    private final ActivityResultLauncher<Input> launcher;
    @Nullable
    private OnActivityResult<Result> onActivityResult;

    private CustomActivityResult(@NonNull ActivityResultCaller caller,
                                 @NonNull ActivityResultContract<Input, Result> contract,
                                 @Nullable OnActivityResult<Result> onActivityResult) {
        this.onActivityResult = onActivityResult;
        this.launcher = caller.registerForActivityResult(contract, this::callOnActivityResult);
    }

    public void setOnActivityResult(@Nullable OnActivityResult<Result> onActivityResult) {
        this.onActivityResult = onActivityResult;
    }

    /**
     * Launch activity, same as {@link ActivityResultLauncher#launch(Object)} except that it allows a callback
     * executed after receiving a result from the target activity.
     */
    public void launch(Input input, @Nullable OnActivityResult<Result> onActivityResult) {
        if (onActivityResult != null) {
            this.onActivityResult = onActivityResult;
        }
        launcher.launch(input);
    }

    /**
     * Same as {@link #launch(Object, OnActivityResult)} with last parameter set to {@code null}.
     */
    public void launch(Input input) {
        launch(input, this.onActivityResult);
    }

    private void callOnActivityResult(Result result) {
        if (onActivityResult != null) onActivityResult.onActivityResult(result);
    }
}