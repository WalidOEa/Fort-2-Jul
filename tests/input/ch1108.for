      PROGRAM CH1108
      IMPLICIT LOGICAL (A-Z)
      INTEGER IB1, IB2
      INTEGER N1, N2
      INTEGER LTRIM
      CHARACTER*22 BUFFER, BUFF1, BUFF2
C PROGRAM TO READ A RECORD OF THE FORM
C #XXXXXXXXXX YYYYYYYYYY
C SO THAT INTEGERS N1 = XXXXXXXXXX N2 = YYYYYYYYYY
C WHERE THE NUMBER OF DIGITS VARIES FROM 1 TO 10
C USE INTERNAL FILES
        PRINT *, 'INPUT MICAEL''S NUMBERS'
        READ (*,'(A)') BUFFER
        IB1 = INDEX(BUFFER,' ')
        IB2 = LTRIM(BUFFER)
        BUFF1 = BUFFER(2:IB1-1)
        BUFF2 = BUFFER(IB1+1:IB2)
        READ (BUFF1,'(I10)') N1
        READ (BUFF2,'(I10)') N2
        PRINT *, 'N1 = ', N1
        PRINT *, 'N2 = ', N2
      END

      INTEGER FUNCTION LTRIM(BUFFER)
      IMPLICIT LOGICAL (A-Z)
      CHARACTER*22 BUFFER
      INTEGER I, J
      J=1
      DO 10 I=1,22
        IF (BUFFER(I:I) .EQ. ' ') GOTO 10
        J=J+1
10    CONTINUE
      LTRIM=J
      RETURN
      END