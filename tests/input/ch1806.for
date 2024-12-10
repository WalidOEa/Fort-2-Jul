      SUBROUTINE SOLVE(A,B,N,X)
      INTEGER J,K,L,N
      REAL A,B,X
      DIMENSION A(N,N),B(N),X(N)
C
C SOLVES A SET OF N SIMULTANEOUS EQUATIONS, OF THE FORM
C A(1,1)*X(1) + A(1,2)*X(2) + A(1,3)*X(3) .... = B(1)
C A(2,1)*X(1) + A(2,2)*X(2) + A(2,3)*X(3) .... = B(2)
C
C A(N,1)*X(1) + A(N,2)*X(2) + A(N,3)*X(3) .... = B(N)
C
C INPUT
C THE MATRIX A CONTAINS THE COEFFICIENTS ON THE LHS
C THE VECTOR B CONTAINS THE VALUES ON THE RHS
C OUTPUT
C THE VECTOR X RETURNS THE VALUES AS ABOVE
C NOE THAT THE CONTENTS OF A AND B ARE CHANGED
C
      DO 1 K=1,N
        DO 2 J=K+1,N
          Y=-A(J,K)/A(K,K)
          DO 3 L=K,N
            A(J,L)=A(J,L)+Y*A(K,L)
3         CONTINUE
          B(J)=B(J)+Y*B(K)
2       CONTINUE
1     CONTINUE
C START THE BACK SUBSTITUTION
      X(N)=B(N)/A(N,N)
      DO 4 J=N-1,1,-1
        Y=B(J)
        DO 5 K=J+1,N
          Y=Y-A(J,K)*X(K)
5       CONTINUE
        X(J)=Y/A(J,J)
4     CONTINUE
      END

      PROGRAM C1806A
      REAL AA,BB,XX
      INTEGER I,J,N
      DIMENSION AA(2,2),BB(2),XX(2)
      N=2
      PRINT*,'INPUT ',N,' X ',N,' MATRIX AA ROW BY ROW'
      DO 1 I=1,N
         READ*,(AA(I,J),J=1,N)
1     CONTINUE
      PRINT*,'INPUT ',N,' ELEMENTS OF RHS VECTOR BB'
      READ*,(BB(I),I=1,N)
      CALL SOLVE(AA,BB,N,XX)
      PRINT*,'SOLUTION IS:'
      DO 2 I=1,N
         PRINT*,XX(I)
2     CONTINUE
      END