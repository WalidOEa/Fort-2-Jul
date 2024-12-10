      PROGRAM CH1103
      IMPLICIT LOGICAL (A-Z)
      REAL X

        READ 100, X
 100    FORMAT (E10.0)
        PRINT *, X
        READ 200, X
 200    FORMAT (E10.4)
        PRINT *, X
        READ 300, X
 300    FORMAT (E10.10)
        PRINT *, X
        READ *, X
        PRINT *, X
        READ 100, X
        PRINT *, X
        READ 200, X
        PRINT *, X
        READ 300, X
        PRINT *, X
        READ *, X
        PRINT *, X
      END PROGRAM CH1103