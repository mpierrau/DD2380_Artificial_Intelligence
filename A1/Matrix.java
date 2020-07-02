public class Matrix {

	private int rows;
	private int cols;
	public double[][] mat;

	public Matrix(int row, int col) {
		rows = row;
		cols = col;
		mat = new double[rows][cols];
	}

	public Matrix(String input) {
		String[] split = input.split(" ");
		rows = Integer.parseInt(split[0]);
		cols = Integer.parseInt(split[1]);
		mat = new double[rows][cols];

		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				mat[i][j] = Double.parseDouble(split[j + i * this.cols + 2]);
			}
		}
	}

	public static Matrix multiply(Matrix A, Matrix B) {
		Matrix res = new Matrix(A.rows, B.cols);
		for (int i = 0; i < A.rows; i++) {
			for (int j = 0; j < B.cols; j++) {
				for (int k = 0; k < A.cols; k++) {
					res.mat[i][j] += A.mat[i][k] * B.mat[k][j];
				}
			}
		}
		return res;
	}

	public static Matrix elWiseMultiply(Matrix A, Matrix B) {
		// This function assumes that A and B have the same dimensions
		Matrix res = new Matrix(A.rows, A.cols);
		for (int i = 0; i < A.rows; i++) {
			for (int j = 0; j < A.cols; j++) {
				res.mat[i][j] = A.mat[i][j] * B.mat[i][j];
			}
		}
		return res;
	}

	public static Matrix elWiseDivide(Matrix A, Matrix B) {
		// This function assumes that A and B have the same dimensions
		Matrix res = new Matrix(A.rows, A.cols);
		for (int i = 0; i < A.rows; i++) {
			for (int j = 0; j < A.cols; j++) {
				res.mat[i][j] = A.mat[i][j] / B.mat[i][j];
			}
		}
		return res;
	}

	public static Matrix transpose(Matrix A) {
		Matrix res = new Matrix(A.cols, A.rows);
		for (int i = 0; i < A.rows; i++) {
			for (int j = 0; j < A.cols; j++) {
				res.mat[j][i] = A.mat[i][j];
			}
		}
		return res;
	}

	public Matrix transpose() {
		double[][] temp = new double[this.cols][this.rows];
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				temp[j][i] = this.mat[i][j];
			}
		}
		int t = this.rows;
		this.rows = this.cols;
		this.cols = t;
		this.mat = temp;
		return this;
	}

	@Override
	public String toString() {
		String ret = this.rows + " " + this.cols;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				ret = ret + " " + String.format("%01f",mat[i][j]);
			}
		}
		return ret;
	}

	// Printing matrix without dimensions
	public String toStringNoDim() {
		String ret = "";
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				ret = ret + mat[i][j] + " ";
			}
		}
		return ret;
	}

	public Matrix getColumn(int nr) {
		Matrix ret = new Matrix(this.rows, 1);
		for (int i = 0; i < this.rows; i++) {
			ret.mat[i][0] = this.mat[i][nr];
		}
		return ret;
	}

	public Matrix getRow(int nr) {
		Matrix ret = new Matrix(1, this.cols);
		for (int i = 0; i < this.cols; i++) {
			ret.mat[0][i] = this.mat[nr][i];
		}
		return ret;
	}

	public double sum() {
		double sum = 0;
		for (int i = 0; i < rows; i++) {
			for (int j = 0; j < cols; j++) {
				sum += mat[i][j];
			}
		}
		return sum;
	}

	public Matrix addColumn(Matrix A) {
		this.cols++;
		double[][] temp = new double[this.rows][this.cols];
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols - 1; j++) {
				temp[i][j] = this.mat[i][j];
			}
		}
		for (int i = 0; i < A.rows; i++) {
			temp[i][this.cols - 1] = A.mat[i][0];
		}
		this.mat = temp;
		return this;
	}

	public Matrix addRow(Matrix A) {
		// Adds one row at bottom of this.matrix with elements in A
		// A must have 1 X A.ncols() dimensions
		this.rows++;
		double[][] temp = new double[this.rows][this.cols];
		for (int i = 0; i < this.rows - 1; i++) {
			for (int j = 0; j < this.cols; j++) {

				temp[i][j] = this.mat[i][j];
			}
		}
		for (int i = 0; i < A.cols; i++) {
			temp[this.rows - 1][i] = A.mat[0][i];
		}
		this.mat = temp;
		return this;
	}

	public Matrix addPrevColumn(Matrix A) {
		// THIS FUNCTION ADDS ALL COLUMNS IN MATRIX A TO THIS.MATRIX TO THE LEFT OF
		// EXISTING COLUMNS
		this.cols += A.ncols();
		double[][] temp = new double[this.rows][this.cols];

		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < A.cols; j++) {
				temp[i][j] = A.mat[i][j];
			}
		}

		for (int i = 0; i < this.rows; i++) {
			for (int j = A.cols; j < this.cols; j++) {
				temp[i][j] = this.mat[i][j - 1];

			}
		}
		this.mat = temp;
		return this;
	}

	public int argmaxCol(int column_nr) {
		double[] col = new double[this.rows];
		for (int i = 0; i < this.rows; i++) {
			col[i] = this.mat[i][column_nr];
		}

		double max = Integer.MIN_VALUE;
		int idx = 0;
		for (int i = 0; i < col.length; i++) {
			if (col[i] > max) {
				max = col[i];
				idx = i;
			}
		}
		return idx;
	}
	
	public int argmaxRow(int row_nr) {
		double[] row = new double[this.cols];
		for (int i = 0; i < this.cols; i++) {
			row[i] = this.mat[row_nr][i];
		}

		double max = Integer.MIN_VALUE;
		int idx = 0;
		for (int i = 0; i < row.length; i++) {
			if (row[i] > max) {
				max = row[i];
				idx = i;
			}
		}
		return idx;
	}

	public double max(int column_nr) {
		double[] col = new double[this.rows];
		for (int i = 0; i < this.rows; i++) {
			col[i] = this.mat[i][column_nr];
		}

		double max = Integer.MIN_VALUE;
		for (int i = 0; i < col.length; i++) {
			if (col[i] > max) {
				max = col[i];
			}
		}
		return max;
	}

	public Matrix scalMult(double n) {
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				this.mat[i][j] *= n;
			}
		}
		return this;
	}

	public Matrix scalAdd(double n) {
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				this.mat[i][j] += n;
			}
		}
		return this;
	}

	public int nrows() {
		return this.rows;
	}

	public int ncols() {
		return this.cols;
	}

	public Matrix add(Matrix A) {
		// Assumes two matrices of same size
		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				this.mat[i][j] += A.mat[i][j];
			}
		}
		return this;
	}

	public Matrix lnMat() {
		// Returns the matrix with all elements natural logarithmised
		Matrix newMat = new Matrix(this.nrows(), this.ncols());

		for (int i = 0; i < this.rows; i++) {
			for (int j = 0; j < this.cols; j++) {
				newMat.mat[i][j] = Math.log(this.mat[i][j]);
			}
		}
		return newMat;
	}

	
	
	public Matrix scaleColumn(int column_nr, double scaleFactor) {
		for (int i = 0; i < this.rows; i++) {
			this.mat[i][column_nr] = scaleFactor * this.mat[i][column_nr];
		}
		return this;
	}

	public Matrix scaleRow(int row_nr, double scaleFactor) {
		for (int i = 0; i < this.cols; i++) {
			this.mat[row_nr][i] = scaleFactor * this.mat[row_nr][i];
		}
		return this;
	}
	
	public void setColumn(int column_nr, Matrix col) {
		for (int i = 0; i < this.rows; i++) {
			this.mat[i][column_nr] = col.mat[i][0];
		}
	}

	public double compareVar(Matrix matrix){
		double var = 0;
		for(int i=0; i < this.rows; i++){
			for(int j=0; j < this.cols; j++){
				var += Math.pow(this.mat[i][j] - matrix.mat[i][j],2);
			}
		}
		return var/(this.nrows()*this.ncols()-1);
	}

	public double compareMax(Matrix matrix){
		double max = 0;
		double tmpMax;
		for(int i=0; i < this.rows; i++){
			for(int j=0; j < this.cols; j++){
				tmpMax = Math.abs(this.mat[i][j] - matrix.mat[i][j]);
				if(tmpMax > max) max = tmpMax;
			}
		}

		return max;
	}

}
