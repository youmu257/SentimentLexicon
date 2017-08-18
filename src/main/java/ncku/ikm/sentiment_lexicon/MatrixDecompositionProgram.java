package ncku.ikm.sentiment_lexicon;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

// library of public matrix methods including decomposition, inverse, determinant
class MatrixDecompose{
	int[] perm;
    int toggle;
    double[][] luMatrix;
    public MatrixDecompose(int[] p, int t, double[][] lum) {
    	perm = p;
    	toggle = t;
    	luMatrix = lum;
	}
}

class MatrixDecompositionProgram{
	// public matrix methods - consider placing in a class.
    public double[][] MatrixRead(String Filename, int rows, int columns)
    {
        double[][] result = new double[rows][columns];
        try{
            BufferedReader br = new BufferedReader(new FileReader(Filename));
            int count = 0;
            String lin = "";
            while ((lin = br.readLine()) != null)
            {
            	String[] slin = lin.split(","); 
            	for(int i=0;i<slin.length;i++)
            		result[count][i] = Double.parseDouble(slin[i]);
                count++;
            } br.close();
            System.out.println("Reading\n" + Filename + " is done!");
        }catch(IOException e){}
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------
    public double[][] Row_Normalized(double[][] matrix)//�bPropagation�����ϥΡA���Q���Ѱ_��
    {
        double[][] result = new double[matrix.length][matrix[0].length];
        for (int i = 0; i < result.length; i++)
        {
            double sum = Sum(matrix[i]);
            for (int j = 0; j < result[0].length; j++)
            {
                if (sum != 0.0)
                    result[i][j] = matrix[i][j] / sum;
                else
                    result[i][j] = 0.0;
            }
        }
        //System.out.println("After Matrix Row-Normalizing...\n" + MatrixAsString(result));
        return result;
    }

    public double[][] Columm_Normalized(double[][] matrix)
    {
        double[][] result = new double[matrix.length][matrix.length];
        double[] columnSum = new double[matrix.length];
        for (int i = 0; i < matrix.length; i++)
        {
            for (int j = 0; j < matrix.length; j++)
                columnSum[i] += Math.abs(matrix[j][i]);
        }
        for (int i = 0; i < matrix.length; i++)
        {
            for (int j = 0; j < matrix.length; j++)
            {
                if (columnSum[j] != 0)
                    result[i][j] = matrix[i][j] / columnSum[j];
                else
                    result[i][j] = 0;
            }
        }
        return result;
    }

   // --------------------------------------------------------------------------------------------------------------
    public double[][] MatrixIdentity(int n)
    {
        // return an n x n Identity matrix
        double[][] result =  new double[n][n];
        for (int i = 0; i < n; ++i)
            result[i][i] = 1.0;

        return result;
    }

    // --------------------------------------------------------------------------------------------------------------
    public String MatrixAsString(double[][] matrix, int round)
    {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < matrix.length; ++i)
        {
            for (int j = 0; j < matrix[i].length; ++j)
            	s.append(String.format("%8."+round+"f ", matrix[i][j]));//�������8���,�p���I������ round ��
            s.append("\n");
        }
        return s.toString();
    }

    // --------------------------------------------------------------------------------------------------------------
    public static String MatrixAsString(double[][] matrix)
    {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < matrix.length; ++i)
        {
            for (int j = 0; j < matrix[i].length; ++j)
            	s.append(String.format("%8.5f ",matrix[i][j]));
            s.append("\n");
        }
        return s.toString();
    }

    // --------------------------------------------------------------------------------------------------------------
    public static boolean MatrixAreEqual(double[][] matrixA, double[][] matrixB, double epsilon)
    {
        // true if all values in matrixA == corresponding values in matrixB
        int aRows = matrixA.length; int aCols = matrixA[0].length;
        int bRows = matrixB.length; int bCols = matrixB[0].length;
        if (aRows != bRows || aCols != bCols){
           // throw new Exception("Non-conformable matrices in MatrixAreEqual");
        	System.out.println("Non-conformable matrices in MatrixAreEqual");
        	System.exit(1); 
        }

        for (int i = 0; i < aRows; ++i) // each row of A and B
            for (int j = 0; j < aCols; ++j) // each col of A and B
            {
                double temp = Math.abs(matrixA[i][j] - matrixB[i][j]);
                //if (matrixA[i][j] != matrixB[i][j])
                if (temp > epsilon)
                {
//                        System.out.println(i + "\t" + j + "\t" + temp);
                    return false;
                }
            }
        return true;
    }

    // --------------------------------------------------------------------------------------------------------------
    public static double[][] MatrixProduct(double[][] matrixA, double[][] matrixB)
    {
        int aRows = matrixA.length; int aCols = matrixA[0].length;
        int bRows = matrixB.length; int bCols = matrixB[0].length;
        if (aCols != bRows){
        	System.out.println("Non-conformable matrices in MatrixProduct");
        	System.exit(1); 
        }

        double[][] result =  new double[aRows][bCols];

        for (int i = 0; i < aRows; ++i) // each row of A
            for (int j = 0; j < bCols; ++j) // each col of B
                for (int k = 0; k < aCols; ++k) // could use k < bRows
                    result[i][j] += matrixA[i][k] * matrixB[k][j];
        return result;
    }

    public double[][] MatrixDiagonalProduct(double[][] matrixA, double[][] matrixB)//�S���ϥΨ�
    {
        int aRows = matrixA.length; int aCols = matrixA[0].length;
        int bRows = matrixB.length; int bCols = matrixB[0].length;
        if (aCols != bRows){
        	System.out.println("Non-conformable matrices in MatrixProduct");
        	System.exit(1);
        }

        double[][] result =  new double[aRows][bCols];

        for (int i = 0; i < aRows; ++i) // each row of A
            for (int j = 0; j < bCols; ++j) // each col of B
                result[i][j] = matrixA[i][i] * matrixB[i][j];
        return result;
    }

    public double[][] MatrixProductDiagonal(double[][] matrixA, double[][] matrixB)//�S���ϥΨ�
    {
        int aRows = matrixA.length; int aCols = matrixA[0].length;
        int bRows = matrixB.length; int bCols = matrixB[0].length;
        if (aCols != bRows){
        	System.out.println("Non-conformable matrices in MatrixProduct");
        	System.exit(1);
        }

        double[][] result =  new double[aRows][bCols];

        for (int i = 0; i < aRows; ++i) // each row of A
            for (int j = 0; j < bCols; ++j) // each col of B
                result[i][j] = matrixA[i][j] * matrixB[i][i];
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------
    public double[][] MatrixProduct(double[][] matrixA, double number)//������matrixA�C�Ӥ��������Hnumber
    {
        int aRows = matrixA.length; int aCols = matrixA[0].length;

        double[][] result =  new double[aRows][aCols];

        for (int i = 0; i < aRows; ++i) // each row of A
            for (int j = 0; j < aCols; ++j) // could use k < bRows
                result[i][j] = matrixA[i][j] * number;
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------
    public double[][] MatrixMinus(double[][] matrixA, double[][] matrixB)//�x�}�۴�
    {
        int aRows = matrixA.length; int aCols = matrixA[0].length;
        int bRows = matrixB.length; int bCols = matrixB[0].length;
        if (aCols != bCols || aRows != bRows){
        	System.out.println("Non-conformable matrices in MatrixProduct");
        	System.exit(1);
        }

        double[][] result =  new double[aRows][aCols];

        for (int i = 0; i < aRows; i++) // each row of A
            for (int j = 0; j < bCols; j++) // each col of B
                result[i][j] = matrixA[i][j] - matrixB[i][j];
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------       
    public double[][] MatrixPlus(double[][] matrixA, double[][] matrixB)//�x�}�ۥ[
    {
        int aRows = matrixA.length; int aCols = matrixA[0].length;
        int bRows = matrixB.length; int bCols = matrixB[0].length;
        if (aCols != bCols || aRows != bRows){
        	System.out.println("Non-conformable matrices in MatrixProduct");
        	System.exit(1);
        }

        double[][] result =  new double[aRows][aCols];

        for (int i = 0; i < aRows; i++) // each row of A
            for (int j = 0; j < bCols; j++) // each col of B
                result[i][j] = matrixA[i][j] + matrixB[i][j];
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------
    public double[] MatrixVectorProduct(double[][] matrix, double[] vector)//�G���x�}�M�@���x�}�ۭ�
    {
        // result of multiplying an n x m matrix by a m x 1 column vector (yielding an n x 1 column vector)
        int mRows = matrix.length; int mCols = matrix[0].length;
        int vRows = vector.length;
        if (mCols != vRows){
        	System.out.println("Non-conformable matrix and vector in MatrixVectorProduct");
        	System.exit(1);
        }
        double[] result = new double[mRows]; // an n x m matrix times a m x 1 column vector is a n x 1 column vector
        for (int i = 0; i < mRows; ++i)
            for (int j = 0; j < mCols; ++j)
                result[i] += matrix[i][j] * vector[j];
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------
    public static  MatrixDecompose MatrixDecompose(double[][] matrix, int[] perm, int toggle)
    {   //���^��3�ӰѼ�perm,toggle,result�A�ҥH�^�Ǥ@�� class MatrixDecompose
        // Doolittle LUP decomposition with partial pivoting.
        // rerturns: result is L (with 1s on diagonal) and U; perm holds row permutations; toggle is +1 or -1 (even or odd)
        int rows = matrix.length;
        int cols = matrix[0].length; // assume all rows have the same number of columns so just use row [0].
        if (rows != cols){
        	System.out.println("Attempt to MatrixDecompose a non-square mattrix");
        	System.exit(1);//�פ�{��
        }

        int n = rows; // convenience

       // double[][] result = MatrixDuplicate(matrix); // make a copy of the input matrix
        double[][] result;
        result = Arrays.copyOf(matrix, matrix.length);

        perm = new int[n]; // set up row permutation result
        for (int i = 0; i < n; ++i) { perm[i] = i; }

        toggle = 1; // toggle tracks row swaps. +1 -> even, -1 -> odd. used by MatrixDeterminant

        for (int j = 0; j < n - 1; ++j) // each column
        {
            double colMax = Math.abs(result[j][j]); // j ��̤j����
            int pRow = j;
            for (int i = j + 1; i < n; ++i)         // ��� j ��̤j����
            {
                if (result[i][j] > colMax)
                {
                    colMax = result[i][j];
                    pRow = i;
                }
            }

            if (pRow != j) // if largest value not on pivot, swap rows
            {
                double[] rowPtr = result[pRow];// j ��P�̤j�Ȫ����@��洫
                result[pRow] = result[j];
                result[j] = rowPtr;

                int tmp = perm[pRow]; // and swap perm info
                perm[pRow] = perm[j];
                perm[j] = tmp;

                toggle = -toggle; // adjust the row-swap toggle
            }

            if (Math.abs(result[j][j]) < 1.0E-20) // if diagonal after swap is zero . . .
                return null; // consider a throw

            for (int i = j + 1; i < n; ++i)
            {
                result[i][j] /= result[j][j];
                for (int k = j + 1; k < n; ++k)
                {
                    result[i][k] -= result[i][j] * result[j][k];
                }
            }
        } // main j column loop
        return new MatrixDecompose(perm,toggle,result);
    } // MatrixDecompose

    // --------------------------------------------------------------------------------------------------------------
    public static double[][] MatrixInverse(double[][] matrix)
    {
        int n = matrix.length;
        double[][] result = MatrixDuplicate(matrix);

        MatrixDecompose MD = MatrixDecompose(matrix, new int[0], 0);
        
        if (MD.luMatrix == null){
        	System.out.println("Unable to compute inverse");
        	System.exit(1);
        }

        double[] b = new double[n];
        for (int i = 0; i < n; ++i)
        {
            for (int j = 0; j < n; ++j)
            {
                if (i == MD.perm[j])
                    b[j] = 1.0;
                else
                    b[j] = 0.0;
            }

            double[] x = HelperSolve(MD.luMatrix, b);

            for (int j = 0; j < n; ++j)
                result[j][i] = x[j];
        }
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------
    public static double MatrixDeterminant(double[][] matrix)
    {
        MatrixDecompose MD = MatrixDecompose(matrix,new int[0], 0);

        if (MD.luMatrix == null){
        	System.out.println("Unable to compute MatrixDeterminant");
        	System.exit(1);
        }
        double result = MD.toggle;
        for (int i = 0; i < MD.luMatrix.length; ++i)
            result *= MD.luMatrix[i][i];
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------
    public static double[] HelperSolve(double[][] luMatrix, double[] b) // helper
    {
        // before calling this helper, permute b using the perm array from MatrixDecompose that generated luMatrix
        int n = luMatrix.length;
        double[] x = new double[n];
        System.arraycopy(b,0,x,0,b.length);//copy b to x
        for (int i = 1; i < n; ++i)
        {
            double sum = x[i];
            for (int j = 0; j < i; ++j)
                sum -= luMatrix[i][j] * x[j];
            x[i] = sum;
        }

        x[n - 1] /= luMatrix[n - 1][n - 1];
        for (int i = n - 2; i >= 0; --i)
        {
            double sum = x[i];
            for (int j = i + 1; j < n; ++j)
                sum -= luMatrix[i][j] * x[j];
            x[i] = sum / luMatrix[i][i];
        }

        return x;
    }

    // --------------------------------------------------------------------------------------------------------------
    public static double[] SystemSolve(double[][] A, double[] b)
    {
        // Solve Ax = b
        int n = A.length;

        // 1. decompose A
        MatrixDecompose MD = MatrixDecompose(A,  new int[0], 0);
        
        //double[][] luMatrix = MD.result;
        if (MD.luMatrix == null)
            return null;

        // 2. permute b according to perm[] into bp
        double[] bp = new double[b.length];
        for (int i = 0; i < n; ++i)
            bp[i] = b[MD.perm[i]];

        // 3. call helper
        double[] x = HelperSolve(MD.luMatrix, bp);
        return x;
    } // SystemSolve

    // --------------------------------------------------------------------------------------------------------------
   public static double[][] MatrixDuplicate(double[][] matrix)
    {
        // allocates/creates a duplicate of a matrix. assumes matrix is not null.
        double[][] result =  new double[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; ++i) // copy the values
            for (int j = 0; j < matrix[0].length; ++j)
                result[i][j] = matrix[i][j];
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------

    public static double[][] ExtractLower(double[][] matrix)
    {
        // lower part of a Doolittle decomposition (1.0s on diagonal, 0.0s in upper)
        int rows = matrix.length; int cols = matrix[0].length;
        double[][] result =  new double[rows][cols];
        for (int i = 0; i < rows; ++i)
        {
            for (int j = 0; j < cols; ++j)
            {
                if (i == j)
                    result[i][j] = 1.0;
                else if (i > j)
                    result[i][j] = matrix[i][j];
            }
        }
        return result;
    }

    public static double[][] ExtractUpper(double[][] matrix)
    {
        // upper part of a Doolittle decomposition (0.0s in the strictly lower part)
        int rows = matrix.length; int cols = matrix[0].length;
        double[][] result =  new double[rows][cols];
        for (int i = 0; i < rows; ++i)
        {
            for (int j = 0; j < cols; ++j)
            {
                if (i <= j)
                    result[i][j] = matrix[i][j];
            }
        }
        return result;
    }

    // --------------------------------------------------------------------------------------------------------------

    public static double[][] PermArrayToMatrix(int[] perm)
    {   
        // convert Doolittle perm array to corresponding perm matrix
        int n = perm.length;
        double[][] result =  new double[n][n];
        for (int i = 0; i < n; ++i)
            result[i][perm[i]] = 1.0;
        return result;
    }

    public static double[][] UnPermute(double[][] luProduct, int[] perm)
    {
        // unpermute product of Doolittle lower * upper matrix according to perm[]
        // no real use except to demo LU decomposition, or for consistency testing
        double[][] result = MatrixDuplicate(luProduct);

        int[] unperm = new int[perm.length];
        for (int i = 0; i < perm.length; ++i)
            unperm[perm[i]] = i;

        for (int r = 0; r < luProduct.length; ++r)
            result[r] = luProduct[unperm[r]];

        return result;
    } // UnPermute
    
   public static double Sum(double[] arr){
    	double sum = 0;
    	for(int i=0;i<arr.length;i++)
    		sum += arr[i];
    	return sum;
    }
    // --------------------------------------------------------------------------------------------------------------
} // class MatrixDecompositionProgram
