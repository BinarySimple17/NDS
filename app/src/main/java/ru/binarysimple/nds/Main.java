package ru.binarysimple.nds;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.Currency;
import java.util.Locale;

public class Main extends AppCompatActivity {

    private final String TAX18 = "18.00";
    private final String TAX10 = "10.00";
    private static final String LOG_TAG = "nd_log";
    private final static String SAVE_TV_GROSS = "SAVE_TV_GROSSN";
    private final static String SAVE_TV_NET = "SAVE_TV_NET";
    private final static String SAVE_TV_TAX = "SAVE_TV_TAX";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //ADS
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // извлекаем данные
        TextView tvGross = (TextView) findViewById(R.id.tvGross);
        TextView tvTax = (TextView) findViewById(R.id.tvTax);
        TextView tvNet = (TextView) findViewById(R.id.tvNet);
        if (savedInstanceState != null) {
            tvGross.setText(savedInstanceState.getString(SAVE_TV_GROSS, getResources().getString(R.string.zero)));
            tvTax.setText(savedInstanceState.getString(SAVE_TV_TAX, getResources().getString(R.string.zero)));
            tvNet.setText(savedInstanceState.getString(SAVE_TV_NET, getResources().getString(R.string.zero)));
        }


        RadioGroup radiogroup = (RadioGroup) findViewById(R.id.rgTaxes);

        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                EditText etTaxRate = (EditText) findViewById(R.id.etTaxrate);
                RadioButton rbOther = (RadioButton) findViewById(R.id.rbOther);
                etTaxRate.setEnabled(rbOther.isChecked());

                // TODO Auto-generated method stub
                switch (checkedId) {
                    case -1:
                        break;
                    case R.id.rbUsual13:
                        break;
                    case R.id.rbNonresident30:
                        break;
                    case R.id.rbOther:
                        break;
                    default:
                        break;
                }
            }
        });

        Button btnCalc = (Button) findViewById(R.id.button);

        btnCalc.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                calc();
            }
        });
    }

    private void calc() {
        RadioGroup rgBase = (RadioGroup) findViewById(R.id.rgBase);
        RadioGroup rgTaxes = (RadioGroup) findViewById(R.id.rgTaxes);
        TextView tvGross = (TextView) findViewById(R.id.tvGross);
        TextView tvTax = (TextView) findViewById(R.id.tvTax);
        TextView tvNet = (TextView) findViewById(R.id.tvNet);
        EditText etBase = (EditText) findViewById(R.id.etBase);
        EditText etTaxrate = (EditText) findViewById(R.id.etTaxrate);
        String taxRate = "0";

        switch (rgTaxes.getCheckedRadioButtonId()) {
            case -1:
                break;
            case R.id.rbUsual13:
                taxRate = TAX18;
                break;
            case R.id.rbNonresident30:
                taxRate = TAX10;
                break;
            case R.id.rbOther:
                taxRate = etTaxrate.getText().toString();
                break;
            default:
                break;
        }

        if ((CurrOps.zero(taxRate)) || CurrOps.zero(etBase.getText().toString())) {
            setToZero();
            return;
        }

        switch (rgBase.getCheckedRadioButtonId()) {
            case -1:
                break;
            case R.id.rbSumNet: //сумма с вычетом налога
/*                Если у Вас под рукой не окажется интернета и надо будет выделить
                НДС - запомните этот простой алгоритм. Чтобы выделить НДС из суммы,
                    нужно разделить сумму на 1+НДС (т.е. если НДС 18%, то разделить
                    нужно на 1.18), вычесть из полученного исходную сумму, умножить
                на -1 и округлить до копеек в ближайшую сторону. Если вы делаете это
                на калькуляторе, то последние два действия легко выполнить в уме.*/

                String taxSumN = calcGrossN(taxRate); //return tax (BASE*100)/100+18
                //String tax = calcTaxNet(base); //return full sum, then fill taxes.
                tvGross.setText(convertC(etBase.getText().toString()));//общая сумма с налогом
                tvTax.setText(taxSumN);//сумма налога
                tvNet.setText(calcBaseNet(taxSumN));//сумма (база)

                break;
            case R.id.rbsumGross: //введена общая сумма
                String taxSumG = calcTax(etBase.getText().toString(), taxRate); //сумма налога √
                String give = calcGross(etBase.getText().toString(),taxSumG); //общая сумма с налогом
                tvGross.setText(give); //общая сумма с налогом
                tvTax.setText(taxSumG); //сумма налога
                tvNet.setText(convertC(etBase.getText().toString())); //сумма (база)
                break;
            default:
                break;
        }
    }

    private void setToZero() {
        TextView tvGross = (TextView) findViewById(R.id.tvGross);
        TextView tvTax = (TextView) findViewById(R.id.tvTax);
        TextView tvNet = (TextView) findViewById(R.id.tvNet);
        tvGross.setText(getResources().getText(R.string.zero));
        tvTax.setText(getResources().getText(R.string.zero));
        tvNet.setText(getResources().getText(R.string.zero));
    }

    private String calcBaseNet(String taxSum) {
        EditText etBase = (EditText) findViewById(R.id.etBase);
        Currency curr = Currency.getInstance(Locale.getDefault());
        return CurrOps.sub(curr,etBase.getText().toString(),taxSum);
    }

    private String convertC(String base) {
        Currency curr = Currency.getInstance(Locale.getDefault());
        return CurrOps.convertToCurr(curr, base);
    }

    private String calcGross(String base, String taxSum) {
        Currency curr = Currency.getInstance(Locale.getDefault());
        return CurrOps.add(curr,base,taxSum);
    }

    private String calcGrossN(String taxRate) {
        /*                Если у Вас под рукой не окажется интернета и надо будет выделить
                НДС - запомните этот простой алгоритм. Чтобы выделить НДС из суммы,
                    нужно разделить сумму на 1+НДС (т.е. если НДС 18%, то разделить
                    нужно на 1.18), вычесть из полученного исходную сумму, умножить
                на -1 и округлить до копеек в ближайшую сторону. Если вы делаете это
                на калькуляторе, то последние два действия легко выполнить в уме.*/

        //return tax (BASE*100)/100+18
        EditText etBase = (EditText) findViewById(R.id.etBase);
        Currency curr = Currency.getInstance(Locale.getDefault());
        String top = CurrOps.mult(curr, etBase.getText().toString(), taxRate);
        String bottom = CurrOps.add(curr, "100", taxRate);
        String base = CurrOps.div(curr, top, bottom);
        return CurrOps.sub(curr,etBase.getText().toString(),base);
    }

    private String calcTax(String base, String tax) {
        Currency curr = Currency.getInstance(Locale.getDefault());
        return CurrOps.mult(curr, base, tax);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(LOG_TAG, "onSaveInstanceState");
        TextView tvGross = (TextView) findViewById(R.id.tvGross);
        TextView tvTax = (TextView) findViewById(R.id.tvTax);
        TextView tvNet = (TextView) findViewById(R.id.tvNet);
        outState.putString(SAVE_TV_GROSS, tvGross.getText().toString());
        outState.putString(SAVE_TV_NET, tvNet.getText().toString());
        outState.putString(SAVE_TV_TAX, tvTax.getText().toString());
    }


}
