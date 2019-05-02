package br.ufg.emc.termografia.diagnosis;

import android.content.Context;

import androidx.core.content.ContextCompat;

import br.ufg.emc.termografia.R;

public enum Concept {
    A, B, C, D, E;

    public int getColor(Context context) {
        switch (this) {
            case A: return ContextCompat.getColor(context, R.color.concept_a);
            case B: return ContextCompat.getColor(context, R.color.concept_b);
            case C: return ContextCompat.getColor(context, R.color.concept_c);
            case D: return ContextCompat.getColor(context, R.color.concept_d);
            case E: return ContextCompat.getColor(context, R.color.concept_e);
            default: return ContextCompat.getColor(context, R.color.gray_800);
        }
    }
}
