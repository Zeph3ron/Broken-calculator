package dk.kea.class2017.kristinn.brokencalculator;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;

import static android.os.Build.VERSION_CODES.M;
import static dk.kea.class2017.kristinn.brokencalculator.MainActivity.SwitchMode.Value;

public class MainActivity extends AppCompatActivity
{
    //A single variable to make sure the corret context is called.
    final Context context = this;

    //String displayed in Input Box
    public String inputString;

    //String displayed above the Input Box
    public String resultString;

    //The selected button (the one that has turned green/yellow)
    public Button selectedButton;

    //Represents the current Switch Mode
    //Player can switch either the 'Value' of the buttons OR the actual 'Position'
    public SwitchMode switchMode;

    //These become true after each puzzle/element has been activated and/or solved.
    public boolean firstActivated;
    public boolean firstSolved;
    public boolean secondActivated;
    public boolean secondSolved;
    public boolean switchButtonActivated;

    //List of all the buttons, used to keep track of their positions when switching orientation
    public ArrayList<Button> buttonList;

    //Used to keep track of the symbol that should be shown if the weird button has been switched
    public String weirdButtonValue;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonList = getButtons();
        if (savedInstanceState == null)
        {
            inputString = "";
            resultString = "";
            switchMode = SwitchMode.Position;
            showWelcomeDialogs();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        outState.putString("inputString", inputString);
        outState.putString("resultString", resultString);
        outState.putString("weirdButtonValue", weirdButtonValue);
        outState.putBoolean("firstActivated", firstActivated);
        outState.putBoolean("firstSolved", firstSolved);
        outState.putBoolean("secondActivated", secondActivated);
        outState.putBoolean("secondSolved", secondSolved);
        outState.putBoolean("switchButtonActivated", switchButtonActivated);
        outState.putSerializable("switchMode", switchMode);
        outState.putSerializable("buttonList", buttonList);
        super.onSaveInstanceState(outState);
    }

    @RequiresApi(api = M)
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);
        inputString = savedInstanceState.getString("inputString");
        resultString = savedInstanceState.getString("resultString");
        weirdButtonValue = savedInstanceState.getString("weirdButtonValue");
        firstActivated = savedInstanceState.getBoolean("firstActivated");
        secondActivated = savedInstanceState.getBoolean("secondActivated");
        firstSolved = savedInstanceState.getBoolean("firstSolved");
        secondSolved = savedInstanceState.getBoolean("secondSolved");
        switchButtonActivated = savedInstanceState.getBoolean("switchButtonActivated");
        switchMode = (SwitchMode) savedInstanceState.getSerializable("switchMode");
        restoreButtons((ArrayList<Button>) savedInstanceState.getSerializable("buttonList"));
        if (firstActivated)
        {
            hookupButtons();
        }
        updateInputView(false);
        updateResultView(false);

        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE && firstActivated){
            switch (switchMode)
            {
                case Position:
                    findViewById(R.id.buttonExtra1).setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.selectedButtonPosition)));
                    break;
                case Value:
                    findViewById(R.id.buttonExtra1).setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.selectedButtonValue)));
                    break;
            }
            if (!switchButtonActivated)
            {
                activateSwitchDialogs();
            }
        }
    }

    //region Calculating Related
    public void inputButtonClick(View view)
    {
        switch (view.getId())
        {
            case R.id.button0:
                inputString += "0";
                break;
            case R.id.button1:
                if (!firstActivated)
                {
                    startFirstPuzzle(true);
                }
                inputString += "1";
                break;
            case R.id.button2:
                inputString += "2";
                break;
            case R.id.button3:
                inputString += "3";
                break;
            case R.id.button4:
                inputString += "4";
                break;
            case R.id.button5:
                inputString += "5";
                break;
            case R.id.button6:
                inputString += "6";
                checkSolved1();
                break;
            case R.id.button7:
                inputString += "7";
                break;
            case R.id.button8:
                inputString += "8";
                break;
            case R.id.button9:
                if (!firstActivated)
                {
                    startFirstPuzzle(false);
                }
                inputString += "9";
                break;
            case R.id.buttonComma:
                inputString += "₪";
                if (!secondActivated)
                {
                    startSecondPuzzle();
                }
                break;
        }
        updateInputView(false);
    }

    public void clearInput(View view)
    {
        inputString = "";
        updateInputView(true);
    }

    public void clearAll(View view)
    {
        inputString = "";
        resultString = "";
        updateResultView(true);
        updateInputView(true);
    }

    public void delete(View view)
    {
        if (inputString != "")
        {
            inputString = inputString.substring(0, inputString.length() - 1);
            updateInputView(false);
        }
    }

    public void negate(View view)
    {
        DecimalFormat format = (DecimalFormat) DecimalFormat.getInstance();
        DecimalFormatSymbols symbols = format.getDecimalFormatSymbols();
        char sep = symbols.getDecimalSeparator();

        //create a new instance
        DecimalFormatSymbols custom = new DecimalFormatSymbols();
        custom.setDecimalSeparator(',');
        format.setDecimalFormatSymbols(custom);
        Number number = null;
        try
        {
            number = format.parse(inputString);
            Double results = number.doubleValue();
            results = results * -1;
            inputString = results.longValue() == results ? "" + results.longValue() : "" + results;
            TextView resultText = (TextView) findViewById(R.id.inputView);
            resultText.setText(inputString);
        } catch (ParseException e)
        {
            e.printStackTrace();
        }
    }

    public void updateResultView(boolean clear)
    {
        TextView tv = (TextView) findViewById(R.id.resultsView);
        if (clear)
        {
            tv.setText("");
        } else
        {
            tv.setText(resultString);
        }
    }

    public void updateInputView(boolean clear)
    {
        TextView tv = (TextView) findViewById(R.id.inputView);
        if (clear)
        {
            tv.setText("");
        } else
        {
            tv.setText(inputString);
        }

    }

    public void calculate(View view)
    {
        TextView resultsView = (TextView) findViewById(R.id.resultsView);
        if (inputString == "")
        {
            if (resultString != "")
            {
                //This block only runs if we are switching an input sign (+, -, /, x)
                StringBuilder newResultAppend = new StringBuilder(resultString);
                newResultAppend.setCharAt(newResultAppend.length() - 2, ((Button) view).getText().charAt(0));
                resultString = newResultAppend.toString();
                resultsView.setText(resultString);
            }
        } else
        {
            //If we reach this point there was something in the input box
            double input = Double.parseDouble(((TextView) findViewById(R.id.inputView)).getText().toString());
            if (resultString == "")
            {
                //There was nothing in the corner. Now we don't calculate only add "input + operation" ("5 + ").
                DecimalFormat df = new DecimalFormat("###.#");
                resultString = df.format(input) + " " + ((Button) view).getText().charAt(0) + " ";
                resultsView.setText(resultString);
                clearInput(view);
                return;
            }

            //Only reaches here if there was something in both input and result.
            //We now finish the calculation, and add the new result plus the new sign
            String[] inputParams = ((TextView) findViewById(R.id.resultsView)).getText().toString().split(" ");
            double resultInput = Double.parseDouble(inputParams[0]);
            char newOperation = ((Button) view).getText().charAt(0);
            char operation = inputParams[1].charAt(0);
            double finalResult = getResults(resultInput, input, operation);
            DecimalFormat df = new DecimalFormat("###.#");
            resultString = df.format(finalResult) + " " + newOperation + " ";
            resultsView.setText(resultString);
        }
        clearInput(view);
    }

    public void equals(View view)
    {
        if (inputString != "" && resultString != "")
        {
            TextView resultView = ((TextView) findViewById(R.id.resultsView));
            String[] inputParams = resultView.getText().toString().split(" ");
            double input = Double.parseDouble(((TextView) findViewById(R.id.inputView)).getText().toString());
            double resultInput = Double.parseDouble(inputParams[0]);
            DecimalFormat df = new DecimalFormat("###.#");
            double finalResult = getResults(resultInput, input, inputParams[1].charAt(0));
            resultString = df.format(finalResult) + "   ";
            resultView.setText(resultString);
        }
        clearInput(view);
    }

    public double getResults(double nr1, double nr2, char operation)
    {
        switch (operation)
        {
            case '+':
                return nr1 + nr2;
            case '-':
                return nr1 - nr2;
            case '/':
                return nr1 / nr2;
            case 'x':
                return nr1 * nr2;
        }
        return 0;
    }
    //endregion

    //region Game Related
    private void hookupButtons()
    {
        View.OnLongClickListener longClickListener = new Button.OnLongClickListener()
        {
            @RequiresApi(api = M)
            @Override
            public boolean onLongClick(View v)
            {
                Button longClickedButton = (Button) v;
                switch(switchMode)
                {
                    case Position:
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                        {
                            if(longClickedButton.getId() == R.id.buttonExtra5)
                            {
                                break;
                            }
                        }
                        if (selectedButton == null)
                        {
                            selectedButton = longClickedButton;
                            selectedButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.selectedButtonPosition)));
                        }
                        else
                        {
                            ViewGroup.LayoutParams oldButtonLayout = longClickedButton.getLayoutParams();
                            ViewGroup.LayoutParams newButtonLayout = selectedButton.getLayoutParams();
                            longClickedButton.setLayoutParams(newButtonLayout);
                            selectedButton.setLayoutParams(oldButtonLayout);
                            selectedButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.legacy_button_normal)));
                            selectedButton = null;
                        }
                        break;
                    case Value:
                        if (selectedButton == null)
                        {
                            selectedButton = longClickedButton;
                            selectedButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.selectedButtonValue)));
                        }
                        else
                        {
                            String oldText = longClickedButton.getText().toString();
                            String newText = selectedButton.getText().toString();
                            longClickedButton.setText(newText);
                            selectedButton.setText(oldText);

                            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                            {
                                if(longClickedButton.getId() == R.id.buttonExtra5||selectedButton.getId() == R.id.buttonExtra5)
                                {
                                    weirdButtonValue = ((Button)findViewById(R.id.buttonExtra5)).getText().toString();
                                }
                            }
                            selectedButton.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.legacy_button_normal)));
                            selectedButton = null;
                        }
                        break;
                }

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkSolved1();
                        checkSolved2();
                    }
                }, 200);
                //returning true here makes sure that the normal click event is not fired also
                return true;
            }
        };
        for (int i = 0; i < buttonList.size();i++)
        {
            Button button = buttonList.get(i);
            if (button != null)
            {
                buttonList.get(i).setOnLongClickListener(longClickListener);
            }
        }
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
        {
            findViewById(R.id.buttonExtra5).setOnLongClickListener(longClickListener);
        }
    }

    public ArrayList<Button> getButtons()
    {
        ArrayList<Button> buttons = new ArrayList<>();
        buttons.add((Button)findViewById(R.id.button0));
        buttons.add((Button)findViewById(R.id.button1));
        buttons.add((Button)findViewById(R.id.button2));
        buttons.add((Button)findViewById(R.id.button3));
        buttons.add((Button)findViewById(R.id.button4));
        buttons.add((Button)findViewById(R.id.button5));
        buttons.add((Button)findViewById(R.id.button6));
        buttons.add((Button)findViewById(R.id.button7));
        buttons.add((Button)findViewById(R.id.button8));
        buttons.add((Button)findViewById(R.id.button9));

        buttons.add((Button)findViewById(R.id.buttonAddition));
        buttons.add((Button)findViewById(R.id.buttonSubtract));
        buttons.add((Button)findViewById(R.id.buttonMultiply));
        buttons.add((Button)findViewById(R.id.buttonDivide));

        buttons.add((Button)findViewById(R.id.buttonNegate));
        buttons.add((Button)findViewById(R.id.buttonDelete));
        buttons.add((Button)findViewById(R.id.buttonClear));
        buttons.add((Button)findViewById(R.id.buttonClearAll));
        buttons.add((Button)findViewById(R.id.buttonComma));
        buttons.add((Button)findViewById(R.id.buttonEquals));
        return buttons;
    }

    public void restoreButtons(ArrayList<Button> oldButtonList)
    {
        int orientation = getResources().getConfiguration().orientation;
        for (int i = 0;i < buttonList.size(); i++)
        {
            Button button = buttonList.get(i);
            Button oldButton = oldButtonList.get(i);
            CharSequence oldButtonText = oldButton.getText();
            if (oldButtonText.equals("₪") && orientation == Configuration.ORIENTATION_LANDSCAPE)
            {
                ((Button)findViewById(R.id.buttonExtra5)).setText(weirdButtonValue);
            }
            button.setLayoutParams(oldButtonList.get(i).getLayoutParams());
            button.setText(oldButtonText);
        }
    }

    public void checkSolved1()
    {
        if (firstActivated && !firstSolved)
        {
            Boolean solved = true;
            for (int i = 0; i < buttonList.size(); i++)
            {
                Button button = buttonList.get(i);
                float row = button.getX() / 246;
                float column = (float) Math.ceil(button.getY() / 244);
                switch (i)
                {
                    case 0:
                        if (row != 1 || column != 4)
                            solved = false;
                        break;
                    case 1:
                        if (row != 0 || column != 3)
                            solved = false;
                        break;
                    case 2:
                        if (row != 1 || column != 3)
                            solved = false;
                        break;
                    case 3:
                        if (row != 2 || column != 3)
                            solved = false;
                        break;
                    case 4:
                        if (row != 0 || column != 2)
                            solved = false;
                        break;
                    case 5:
                        if (row != 1 || column != 2)
                            solved = false;
                        break;
                    case 6:
                        if (row != 2 || column != 2)
                            solved = false;
                        break;
                    case 7:
                        if (row != 0 || column != 1)
                            solved = false;
                        break;
                    case 8:
                        if (row != 1 || column != 1)
                            solved = false;
                        break;
                    case 9:
                        if (row != 2 || column != 1)
                            solved = false;
                        break;
                    case 10:
                        if (row != 3 || column != 0)
                            solved = false;
                        break;
                    case 11:
                        if (row != 3 || column != 1)
                            solved = false;
                        break;
                    case 12:
                        if (row != 3 || column != 2)
                            solved = false;
                        break;
                    case 13:
                        if (row != 3 || column != 3)
                            solved = false;
                        break;
                    case 14:
                        if (row != 2 || column != 4)
                            solved = false;
                        break;
                    case 15:
                        if (row != 2 || column != 0)
                            solved = false;
                        break;
                    case 16:
                        if (row != 0 || column != 0)
                            solved = false;
                        break;
                    case 17:
                        if (row != 1 || column != 0)
                            solved = false;
                        break;
                    case 18:
                        if (row != 0 || column != 4)
                            solved = false;
                        break;
                    case 19:
                        if (row != 3 || column != 4)
                            solved = false;
                        break;
                }
            }
            if (solved)
            {
                firstSolved = true;
                firstPuzzleSolved();
            }
        }
    }

    public void checkSolved2()
    {
        if (secondActivated && !secondSolved)
        {
            Boolean solved = true;
            for (int i = 0; i < buttonList.size(); i++)
            {
                String buttonText = buttonList.get(i).getText().toString();
                switch (i)
                {
                    case 0:
                        if (!buttonText.equals("0"))
                            solved = false;
                        break;
                    case 1:
                        if (!buttonText.equals("1"))
                            solved = false;
                        break;
                    case 2:
                        if (!buttonText.equals("2"))
                            solved = false;
                        break;
                    case 3:
                        if (!buttonText.equals("3"))
                            solved = false;
                        break;
                    case 4:
                        if (!buttonText.equals("4"))
                            solved = false;
                        break;
                    case 5:
                        if (!buttonText.equals("5"))
                            solved = false;
                        break;
                    case 6:
                        if (!buttonText.equals("6"))
                            solved = false;
                        break;
                    case 7:
                        if (!buttonText.equals("7"))
                            solved = false;
                        break;
                    case 8:
                        if (!buttonText.equals("8"))
                            solved = false;
                        break;
                    case 9:
                        if (!buttonText.equals("9"))
                            solved = false;
                        break;
                    case 10:
                        if (!buttonText.equals("+"))
                            solved = false;
                        break;
                    case 11:
                        if (!buttonText.equals("-"))
                            solved = false;
                        break;
                    case 12:
                        if (!buttonText.equals("x"))
                            solved = false;
                        break;
                    case 13:
                        if (!buttonText.equals("/"))
                            solved = false;
                        break;
                    case 14:
                        if (!buttonText.equals("Neg"))
                            solved = false;
                        break;
                    case 15:
                        if (!buttonText.equals("Del"))
                            solved = false;
                        break;
                    case 16:
                        if (!buttonText.equals("C"))
                            solved = false;
                        break;
                    case 17:
                        if (!buttonText.equals("CA"))
                            solved = false;
                        break;
                    case 18:
                        if (!buttonText.equals("₪"))
                            solved = false;
                        break;
                    case 19:
                        if (!buttonText.equals("="))
                            solved = false;
                        break;
                }
            }
            if (solved)
            {
                secondSolved = true;
                secondPuzzleSolved();
            }
        }
    }

    @RequiresApi(api = M)
    public void changeSwitchMode(View view)
    {
        if (!switchButtonActivated)
        {
            return;
        }
        switch (switchMode)
        {
            case Position:
                switchMode = Value;
                view.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.selectedButtonValue)));
                break;
            case Value:
                switchMode = SwitchMode.Position;
                view.setBackgroundTintList(ColorStateList.valueOf(getColor(R.color.selectedButtonPosition)));
                break;
        }
    }

    //region Welcome Dialogs
    public void showWelcomeDialogs()
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.message_dialog);
        TextView dialogTv = (TextView) dialog.findViewById(R.id.text);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButton);
        ImageView image = (ImageView) dialog.findViewById(R.id.icon_image);

        showWelcome1(dialog, dialogTv, dialogButton, image);
    }

    public void showWelcome1(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.welcomeDialog_1));
        dialogButton.setText("Next");
        icon.setImageResource(R.mipmap.normal);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                showWelcome2(dialog, dialogTv, dialogButton, icon);
            }
        });
        dialog.show();
    }

    public void showWelcome2(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.welcomeDialog_2));
        dialogButton.setText("Next");
        icon.setImageResource(R.mipmap.normal);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                showWelcome3(dialog, dialogTv, dialogButton, icon);
            }
        });
        dialog.show();
    }

    public void showWelcome3(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.welcomeDialog_3));
        dialogButton.setText("Next");
        icon.setImageResource(R.mipmap.sad);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                showWelcome4(dialog, dialogTv, dialogButton, icon);
            }
        });
        dialog.show();
    }

    public void showWelcome4(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.welcomeDialog_4));
        dialogButton.setText("Sure");
        icon.setImageResource(R.mipmap.sad);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                showWelcome5(dialog, dialogTv, dialogButton, icon);
            }
        });
        dialog.show();
    }

    public void showWelcome5(final Dialog dialog, TextView dialogTv, Button dialogButton, ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.welcomeDialog_5));
        dialogButton.setText("Ok lets do it!");
        icon.setImageResource(R.mipmap.thumb_up);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //endregion
    //region First Puzzle Dialogs
    public void startFirstPuzzle(boolean clickedOne)
    {
        firstActivated = true;
        hookupButtons();
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.message_dialog);
        TextView dialogTv = (TextView) dialog.findViewById(R.id.text);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButton);
        ImageView image = (ImageView) dialog.findViewById(R.id.icon_image);

        firstPuzzleDialog1(dialog, dialogTv, dialogButton, image, clickedOne);
    }

    public void firstPuzzleDialog1(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon, boolean clickedOne)
    {
        if (clickedOne)
        {
            dialogTv.setText(getResources().getString(R.string.firstPuzzle_startOne));
        } else
        {
            dialogTv.setText(getResources().getString(R.string.firstPuzzle_startNine));
        }
        dialogButton.setText("Next");
        icon.setImageResource(R.mipmap.surprised);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                firstPuzzleDialog2(dialog, dialogTv, dialogButton, icon);
            }
        });
        dialog.show();
    }

    public void firstPuzzleDialog2(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.firstPuzzle_2));
        dialogButton.setText("Ok ill try");
        icon.setImageResource(R.mipmap.normal);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void firstPuzzleSolved()
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.message_dialog);
        TextView dialogTv = (TextView) dialog.findViewById(R.id.text);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButton);
        ImageView image = (ImageView) dialog.findViewById(R.id.icon_image);

        firstSolvedDialog1(dialog, dialogTv, dialogButton, image);
    }
    public void firstSolvedDialog1(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.firstPuzzle_solved_1));
        dialogButton.setText("No problem");
        icon.setImageResource(R.mipmap.thumb_up);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    //endregion
    //region Second Puzzle Dialogs
    public void startSecondPuzzle()
    {
        secondActivated = true;
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.message_dialog);
        TextView dialogTv = (TextView) dialog.findViewById(R.id.text);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButton);
        ImageView image = (ImageView) dialog.findViewById(R.id.icon_image);
        secondPuzzleDialog1(dialog, dialogTv, dialogButton, image);
    }

    public void secondPuzzleDialog1(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.secondPuzzle_1));
        dialogButton.setText("What do you mean?");
        icon.setImageResource(R.mipmap.surprised);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                secondPuzzleDialog2(dialog, dialogTv, dialogButton, icon);
            }
        });
        dialog.show();
    }

    public void secondPuzzleDialog2(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.secondPuzzle_2));
        dialogButton.setText("Thats strange");
        icon.setImageResource(R.mipmap.surprised);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                secondPuzzleDialog3(dialog, dialogTv, dialogButton, icon);
            }
        });
        dialog.show();
    }

    public void secondPuzzleDialog3(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.secondPuzzle_3));
        dialogButton.setText("Obviously!");
        icon.setImageResource(R.mipmap.sad);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                secondPuzzleDialog4(dialog, dialogTv, dialogButton, icon);
            }
        });
        dialog.show();
    }

    public void secondPuzzleDialog4(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.secondPuzzle_4));
        dialogButton.setText("I'll do my best!");
        icon.setImageResource(R.mipmap.sad);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                secondPuzzleDialog5(dialog, dialogTv, dialogButton, icon);
            }
        });
        dialog.show();
    }

    public void secondPuzzleDialog5(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.secondPuzzle_5));
        dialogButton.setText("No problem");
        icon.setImageResource(R.mipmap.thumb_up);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void secondPuzzleSolved()
    {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.message_dialog);
        TextView dialogTv = (TextView) dialog.findViewById(R.id.text);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButton);
        ImageView image = (ImageView) dialog.findViewById(R.id.icon_image);

        secondSolvedDialog1(dialog, dialogTv, dialogButton, image);
    }
    public void secondSolvedDialog1(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.secondPuzzle_solved_1));
        dialogButton.setText("Yes I am!");
        icon.setImageResource(R.mipmap.thumb_up);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    //endregion
    //region Activate Switch Dialogs
    public void activateSwitchDialogs()
    {
        switchButtonActivated = true;
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.message_dialog);
        TextView dialogTv = (TextView) dialog.findViewById(R.id.text);
        Button dialogButton = (Button) dialog.findViewById(R.id.dialogButton);
        ImageView image = (ImageView) dialog.findViewById(R.id.icon_image);

        switchDialog1(dialog, dialogTv, dialogButton, image);
    }

    public void switchDialog1(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.activateSwitch_1));
        dialogButton.setText("Im not sure");
        icon.setImageResource(R.mipmap.surprised);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
                switchDialog2(dialog, dialogTv, dialogButton, icon);
            }
        });
        dialog.show();
    }

    public void switchDialog2(final Dialog dialog, final TextView dialogTv, final Button dialogButton, final ImageView icon)
    {
        dialogTv.setText(getResources().getString(R.string.activateSwitch_2));
        dialogButton.setText("Ok I will");
        icon.setImageResource(R.mipmap.normal);

        dialogButton.setOnClickListener(new Button.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        dialog.show();
    }
    //endregion
    //endregion

    public enum SwitchMode{
        Position,
        Value
    }

}
