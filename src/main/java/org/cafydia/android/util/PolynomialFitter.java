package org.cafydia.android.util;

import java.util.ArrayList;

/**
 * Created by user on 4/03/15.
 *
 */
public class PolynomialFitter {

    private final int p, rs;
    private long n = 0;
    private double[][] m;
    private double[] mpc;

    public PolynomialFitter(int degree) {
        p = degree + 1;
        rs = 2 * p - 1;
        m = new double[p][p + 1];
        mpc = new double[rs];
    }

    public void addPoint(double x, double y) {
        n++;

        for (int r = 1; r < rs; r++) {
            mpc[r] += Math.pow(x, r);
        }

        m[0][p] += y;
        for (int r = 1; r < p; r++) {
            m[r][p] += Math.pow(x, r) * y;
        }
    }

    public Polynomial getBestFit() {
        final double[] mpcClone = mpc.clone();
        final double[][] mClone = new double[m.length][];
        for (int x = 0; x < mClone.length; x++) {
            mClone[x] = m[x].clone();
        }

        mpcClone[0] += n;

        for (int r = 0; r < p; r++) {
            for (int c = 0; c < p; c++) {
                mClone[r][c] = mpcClone[r + c];
            }
        }

        gjEchelonize(mClone);

        final Polynomial result = new Polynomial(p);

        for (int j = 0; j < p; j++) {
            result.add(j, mClone[j][p]);
        }
        return result;
    }

    private void gjDivide(double[][] A, int i, int j, int m) {
        for (int q = j + 1; q < m; q++) {
            A[i][q] /= A[i][j];
        }
        A[i][j] = 1;
    }

    private void gjEchelonize(double[][] A) {
        final int n = A.length;
        final int m = A[0].length;
        int i = 0;
        int j = 0;
        while (i < n && j < m) {

            int k = i;
            while (k < n && A[k][j] == 0) {
                k++;
            }

            if (k < n) {
                if (k != i) {
                    gjSwap(A, i, j);
                }

                if (A[i][j] != 1) {
                    gjDivide(A, i, j, m);
                }
                gjEliminate(A, i, j, n, m);
                i++;
            }
            j++;
        }
    }

    private void gjEliminate(double[][] A, int i, int j, int n, int m) {
        for (int k = 0; k < n; k++) {
            if (k != i && A[k][j] != 0) {
                for (int q = j + 1; q < m; q++) {
                    A[k][q] -= A[k][j] * A[i][q];
                }
                A[k][j] = 0;
            }
        }
    }

    private void gjSwap(double[][] A, int i, int j) {
        double temp[];
        temp = A[i];
        A[i] = A[j];
        A[j] = temp;
    }


    public static class Polynomial extends ArrayList<Double> {
        public Polynomial(int p) {
            super(p);
        }

        public double getY(double x) {
            double ret = 0;
            for (int p=0; p<size(); p++) {
                ret += get(p)*(Math.pow(x, p));
            }
            return ret;
        }
    }
}