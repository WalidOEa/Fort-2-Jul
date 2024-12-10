
        SUBROUTINE SUB1(X,WROW,WCOL)
          IMPLICIT LOGICAL (A-Z)
          INTEGER WROW
          INTEGER WCOL
          INTEGER X
          DIMENSION X(WROW,WCOL)
          INTEGER R
          INTEGER C
  
          DO 10 R=1,WROW
              PRINT * , (X(R,C),C=1,WCOL)
  10      CONTINUE
            RETURN
          END      
      
      PROGRAM CH1810
        IMPLICIT LOGICAL (A-Z)
        INTEGER MAXROW
        INTEGER MAXCOL
        INTEGER WROW
        INTEGER WCOL
        PARAMETER (MAXROW=5)
        PARAMETER (MAXCOL=5)
        PARAMETER (WROW=2)
        PARAMETER (WCOL=3)
        INTEGER X
        DIMENSION X(MAXROW,MAXCOL)
        INTEGER R
        INTEGER C
        X(1,1) = 1
        X(1,2) = 2
        X(1,3) = 3
        X(2,1) = 4
        X(2,2) = 5
        X(2,3) = 6

        DO 10 R=1,WROW
          PRINT * , (X(R,C),C=1,WCOL)
10      CONTINUE

        PRINT *,' CALL SUB1'

        CALL SUB1(X,WROW,WCOL)

        PRINT *,' WHOLE ARRAY IS'

        DO 20 R=1,MAXROW
          PRINT * , (X(R,C),C=1,MAXCOL)
20      CONTINUE


        END