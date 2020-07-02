import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

public class alphapass {

    static Matrix A;
    static Matrix B;
    static Matrix PI;
    static int[] obs;

    public static Matrix getAlpha(Matrix prevAlpha, Matrix A, Matrix B, Matrix PI, int prevObs) {
    		
    	return Matrix.elWiseMultiply(Matrix.transpose(Matrix.multiply(Matrix.transpose(prevAlpha), A)), B.getColumn(prevObs));
    	
    	}

    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader("/Users/erik/Downloads/kth.ai.hmm1/hmm1_01.in"));
            //BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            A = new Matrix(reader.readLine());
            B = new Matrix(reader.readLine());
            PI = new Matrix(reader.readLine());
            String[] Obs = reader.readLine().split(" ");
            obs = new int[Obs.length];
            for(int i = 0; i< Obs.length; i++){
                obs[i] = Integer.parseInt(Obs[i]);
            }

            reader.close();

            //Initialise
            Matrix alphaMat;
            alphaMat = Matrix.elWiseMultiply(Matrix.transpose(PI), B.getColumn(obs[1]));

            for(int i=1; i< obs.length-1; i++) {
                Matrix newAlpha = getAlpha(alphaMat.getColumn(i-1), A, B, PI, obs[i+1]);
                alphaMat.addColumn(newAlpha);
            }
            
            System.out.printf("%.6f",alphaMat.getColumn(obs.length-2).sum());
                        
        }catch(Exception e){
            System.out.println(e);
        }
    }

}
