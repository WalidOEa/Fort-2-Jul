      SUBROUTINE ADD(A,TOTAL)
      REAL A,TOTAL
      DIMENSION A(10)
      INTEGER I
       TOTAL=0.0
       DO 1 I=1,10
        TOTAL=TOTAL+A(I)
1      CONTINUE
      END       
      PROGRAM C1803A
      REAL X,SUM
      INTEGER I
      DIMENSION X(10)
       PRINT *,'INPUT 10 NUMBERS'
       READ *,(X(I),I=1,10)
       CALL ADD(X,SUM)
       PRINT*,'SUM OF NUMBERS = ',SUM
      END