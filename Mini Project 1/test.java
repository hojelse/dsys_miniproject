public class test {
    public static void main(String[] args){
        int[] corr = {1,2,3,4,5,6,7,8,9,10};
        int[] err = {1,2,5,4,6,7,8,9,10};
        

        int errors = 0;
        int discards = 0;
        int duplicates = 0;
        int reorders = 0;

        int exp = corr[0];
        int bound = err.length;
        for(int i = 0; i < bound; i++){
            System.out.println("Expected "+ exp + " found " + err[i]);
            if(err[i] != exp){
                errors++;
                if(err[i] > exp){ //DISCARD OR REORDER
                    if(err[i+1] < err[i]){ //REORDER
                        System.out.println("Reorder!");
                        reorders++;
                        exp+=2;
                        i++;
                    } else { //DISCARD
                        System.out.println("Discard!");
                        discards++;
                        exp++;
                        i--;
                    }
                } else if(err[i] < exp){ //DUPLICATE
                    System.out.println("Duplicate!");
                    duplicates++;
                }
            } else {
                exp++;
            }
        }

        errors += corr.length - (exp-1);
        discards += corr.length - (exp-1);

        System.out.println("Errors: " + errors);
        System.out.println("Discards: " + discards);
        System.out.println("Duplicates: " + duplicates);
        System.out.println("Reorders: " + reorders);
    }
}
