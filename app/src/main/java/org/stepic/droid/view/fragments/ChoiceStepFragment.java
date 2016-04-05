package org.stepic.droid.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.RadioGroup;

import com.squareup.otto.Subscribe;

import org.stepic.droid.R;
import org.stepic.droid.events.InternetIsEnabledEvent;
import org.stepic.droid.events.attempts.FailAttemptEvent;
import org.stepic.droid.events.attempts.SuccessAttemptEvent;
import org.stepic.droid.events.submissions.FailGettingLastSubmissionEvent;
import org.stepic.droid.events.submissions.FailSubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SubmissionCreatedEvent;
import org.stepic.droid.events.submissions.SuccessGettingLastSubmissionEvent;
import org.stepic.droid.model.Attempt;
import org.stepic.droid.model.Dataset;
import org.stepic.droid.model.Reply;
import org.stepic.droid.util.DpPixelsHelper;
import org.stepic.droid.util.HtmlHelper;
import org.stepic.droid.util.RadioGroupHelper;

import java.util.ArrayList;
import java.util.List;

public class ChoiceStepFragment extends StepWithAttemptsFragment {

    private RadioGroup mChoiceContainer;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = super.onCreateView(inflater, container, savedInstanceState);
        mChoiceContainer = (RadioGroup) ((LayoutInflater) this.getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_choice_attempt, mAttemptContainer, false);
        mAttemptContainer.addView(mChoiceContainer);
        return v;
    }

    /**
     * it is unique for each attempt type
     *
     * @param attempt data of task
     */
    @Override
    protected void showAttempt(Attempt attempt) {
        Dataset dataset = attempt.getDataset();
        if (dataset == null) return;

        List<String> options = dataset.getOptions();
        if (options == null || options.isEmpty()) return;

        mChoiceContainer.removeAllViews();

        for (String option : options) {
            CompoundButton optionViewItem;
            if (dataset.is_multiple_choice()) {
                optionViewItem = new AppCompatCheckBox(getActivity());
            } else {
                optionViewItem = new AppCompatRadioButton(getActivity());
            }
            buildChoiceItem(optionViewItem, option);
        }
    }


    //it is unique for each type
    @Override
    protected void blockUIBeforeSubmit(boolean needBlock) {
        RadioGroupHelper.setEnabled(mChoiceContainer, !needBlock);
    }

    //it is unique for each type of replay
    @Override
    protected Reply generateReply() {
        List<Boolean> options = new ArrayList<>();
        for (int i = 0; i < mChoiceContainer.getChildCount(); i++) {
            CompoundButton view = (CompoundButton) mChoiceContainer.getChildAt(i);
            options.add(view.isChecked());
        }
        return new Reply.Builder()
                .setChoices(options)
                .build();
    }


    @Override
    protected void onRestoreSubmission() {
        Reply reply = mSubmission.getReply();
        if (reply == null) return;

        List<Boolean> choices = reply.getChoices();
        if (choices == null) return;

        for (int i = 0; i < mChoiceContainer.getChildCount(); i++) {
            CompoundButton view = (CompoundButton) mChoiceContainer.getChildAt(i);
            view.setChecked(choices.get(i));
        }
    }

    private void buildChoiceItem(CompoundButton item, String rawText) {
        int dp4 = (int) DpPixelsHelper.convertDpToPixel(4);
        int horizontalPadding = (int) DpPixelsHelper.convertDpToPixel(4);
        RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        item.setLayoutParams(params);
        item.setPadding(horizontalPadding, dp4, horizontalPadding, dp4);
        item.setMinHeight((int)DpPixelsHelper.convertDpToPixel(48));
        String text = HtmlHelper.fromHtml(rawText).toString();
        item.setText(text);
        mChoiceContainer.addView(item);
    }

    @Subscribe
    @Override
    public void onInternetEnabled(InternetIsEnabledEvent enabledEvent) {
        super.onInternetEnabled(enabledEvent);
    }

    @Override
    @Subscribe
    public void onSuccessLoadAttempt(SuccessAttemptEvent e) {
        super.onSuccessLoadAttempt(e);
    }

    @Override
    @Subscribe
    public void onSuccessCreateSubmission(SubmissionCreatedEvent e) {
        super.onSuccessCreateSubmission(e);
    }

    @Override
    @Subscribe
    public void onGettingSubmission(SuccessGettingLastSubmissionEvent e) {
        super.onGettingSubmission(e);
    }

    @Subscribe
    @Override
    public void onFailCreateAttemptEvent(FailAttemptEvent event) {
        super.onFailCreateAttemptEvent(event);
    }

    @Subscribe
    @Override
    public void onFailCreateSubmission(FailSubmissionCreatedEvent event) {
        super.onFailCreateSubmission(event);
    }

    @Subscribe
    public void onFailGettingSubmission(FailGettingLastSubmissionEvent e) {
        super.onFailGettingSubmission(e);
    }
}