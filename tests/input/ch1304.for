      REAL FUNCTION FUN(X,Y,Z)
      REAL X,Y,Z
      FUN = X*Y**Z
      END        
      PROGRAM TRIAL
      REAL A,B,C,V1
      PRINT *,'INPUT A,B & C'
      READ*,A,B,C
      V1 = FUN(A,B,C)
      PRINT *,'V1 = ',V1
      END
