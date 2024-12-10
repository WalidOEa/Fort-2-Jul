      SUBROUTINE MULT(X,Y,Z,FUN)
      REAL X,Y,Z,FUN
        FUN=X*Y**Z
      END
      PROGRAM SIMPLE
      REAL A2,A,B,C,FN,X
        PRINT*,'INPUT A,B,C & X'
        READ *,A,B,C,X
        CALL MULT(A,B,C,FN)
        A2=FN/X
        PRINT*,'FN = ',FN,' A2 = ',A2
      END