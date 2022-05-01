package com.example.hangoverassistent;

import java.lang.Math;

public class Calculation {
    public double sum(double[] a){
        double s = 0;
        for(int i1=0;i1<a.length;i1++){
            s += a[i1];
        }

        return s;
    }

    public double mean(double[] a){
        double s = sum(a);
        double avg = s/a.length;
        return avg;
    }

    public double var(double[] a){
        double mu = mean(a);
        double v = 0;

        for(int i1=0;i1<a.length;i1++) {
            v += Math.pow(a[i1]-mu,2);
        }

        v = v/a.length;

        return v;
    }

    public double correlate(double[] a1, double[] a2){
        double cor = 0;

        int len = Math.min(a1.length,a2.length);
        for(int i1 = 0;i1<len;i1++){
            cor += a1[i1]*a2[i1];
        }
        return cor;
    }

    public double covariance(double[] a1, double[] a2){
        double mu1 = mean(a1);
        double mu2 = mean(a2);

        double cov = 0;
        int len = Math.min(a1.length,a2.length);
        for(int i1 = 0;i1<len;i1++){
            cov += (a1[i1]-mu1)*(a2[i1]-mu2);
        }

        cov = cov/len;

        return cov;
    }

    public double max(double[] a){
        double m = a[0];

        for(int i1=1;i1<a.length;i1++){
            m = Math.max(m,a[i1]);
        }

        return m;
    }

    public double min(double[] a){
        double m = a[0];

        for(int i1=1;i1<a.length;i1++){
            m = Math.min(m,a[i1]);
        }

        return m;
    }

    // m: mean of a; v: variance of a.
    public double[] trend(double[] a, double m, double v){

        double muT = 0, muD = 0, sigmaT = 0, sigmaD = 0;

        int num_slot = 10;
        int slot_len = (int) Math.ceil(a.length/num_slot);
        double[] slot = new double[slot_len];
        double[] mu = new double[num_slot];
        double[] sigma = new double[num_slot];

        int idx_start = slot_len, idx_stop = Math.min(idx_start+slot_len, a.length), idx = 1;

        System.arraycopy(a,0,slot,0,slot_len);
        mu[0] = mean(slot);
        sigma[0] = var(slot);

        while(idx_stop<a.length){
            idx_stop = Math.min(idx_start+slot_len, a.length);

            double[] slot2 = new double[idx_stop-idx_start];
            System.arraycopy(a,idx_start,slot2,0,slot2.length);

            mu[idx] = mean(slot2);
            sigma[idx] = var(slot2);

            muT += Math.abs(mu[idx]-mu[idx-1]);
            muD += Math.abs(m-mu[idx]);
            sigmaT += Math.abs(sigma[idx]-sigma[idx-1]);
            sigmaD += Math.abs(v-sigma[idx]);

//             for(int i1=idx_start;i1<idx_stop;i1++){
//                muT += Math.abs(mu[idx]-mu[idx-1]);
//                muD += Math.abs(m-mu[idx]);
//                sigmaT += Math.abs(sigma[idx]-sigma[idx-1]);
//                sigmaD += Math.abs(v-sigma[idx]);
//            }

            idx_start = idx_stop;
            idx ++;
        }

        double[] tr = {muT, muD, sigmaT, sigmaD};
        return tr;
    }
}
