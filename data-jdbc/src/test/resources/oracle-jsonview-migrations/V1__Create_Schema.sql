CREATE TABLE "TBL_TEACHER" ("ID" NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL PRIMARY KEY,"NAME" VARCHAR(255) NOT NULL);
CREATE TABLE "TBL_ADDRESS" ("ID" NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL PRIMARY KEY,"STREET" VARCHAR(255) NOT NULL, "CITY" VARCHAR(255) NOT NULL);
CREATE TABLE "TBL_STUDENT" ("ID" NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL PRIMARY KEY,"NAME" VARCHAR(255) NOT NULL, "BIRTH_DATE" DATE NOT NULL, AVERAGE_GRADE BINARY_DOUBLE,
 START_DATE_TIME TIMESTAMP(6), ACTIVE NUMBER(1) DEFAULT 1, ADDRESS_ID NUMBER(19) NOT NULL);
CREATE TABLE "TBL_CLASS" ("ID" NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL PRIMARY KEY,"NAME" VARCHAR(255) NOT NULL,"ROOM" VARCHAR(255) NOT NULL,"TIME" TIMESTAMP NOT NULL,"TEACHER_ID" NUMBER(19) NOT NULL);
CREATE TABLE "TBL_STUDENT_CLASSES" ("ID" NUMBER(19) GENERATED BY DEFAULT ON NULL AS IDENTITY NOT NULL PRIMARY KEY, "STUDENT_ID" NUMBER(19) NOT NULL,"CLASS_ID" NUMBER(19) NOT NULL);

ALTER TABLE "TBL_CLASS"
    ADD CONSTRAINT fk_teacher FOREIGN KEY("TEACHER_ID")
        REFERENCES "TBL_TEACHER"("ID");

ALTER TABLE "TBL_STUDENT_CLASSES"
    ADD CONSTRAINT fk_student FOREIGN KEY("STUDENT_ID")
        REFERENCES "TBL_STUDENT"("ID");

ALTER TABLE "TBL_STUDENT_CLASSES"
    ADD CONSTRAINT fk_class FOREIGN KEY("CLASS_ID")
        REFERENCES "TBL_CLASS"("ID");

ALTER TABLE "TBL_STUDENT"
    ADD CONSTRAINT fk_address FOREIGN KEY("ADDRESS_ID")
        REFERENCES "TBL_ADDRESS"("ID");

CREATE OR REPLACE JSON RELATIONAL DUALITY VIEW STUDENT_VIEW AS
SELECT JSON{'_id': TBL_STUDENT.id,
            'name': TBL_STUDENT.name WITH UPDATE,
            'birthDate': TBL_STUDENT.birth_date WITH UPDATE,
            'averageGrade': TBL_STUDENT.AVERAGE_GRADE WITH UPDATE,
            'startDateTime': TBL_STUDENT.START_DATE_TIME,
            'active': TBL_STUDENT.ACTIVE,
            'address': (SELECT JSON{'addressID': TBL_ADDRESS.id,
                                    'street': TBL_ADDRESS.STREET,
                                    'city': TBL_ADDRESS.CITY} FROM TBL_ADDRESS WITH UPDATE WHERE TBL_STUDENT.ADDRESS_ID = TBL_ADDRESS.ID),
            'schedule': [SELECT JSON{'id': TBL_STUDENT_CLASSES.id,
                                     'class': (SELECT JSON{'classID': TBL_CLASS.id,
                                                           'teacher': (SELECT JSON{'teachID': TBL_TEACHER.id,
                                                                                    'teacher': TBL_TEACHER.name
                                                                                  } FROM TBL_TEACHER WITH UPDATE WHERE TBL_CLASS.teacher_id = TBL_TEACHER.id
                                                                        ),
                                                           'room': TBL_CLASS.room,
                                                           'time': TBL_CLASS.time,
                                                           'name': TBL_CLASS.name WITH UPDATE
                                                           } FROM TBL_CLASS WITH UPDATE WHERE TBL_STUDENT_CLASSES.class_id = TBL_CLASS.id
                                               )
                                    } FROM TBL_STUDENT_CLASSES WITH INSERT UPDATE DELETE WHERE TBL_STUDENT.id = TBL_STUDENT_CLASSES.student_id
                        ]
            } FROM TBL_STUDENT WITH UPDATE INSERT DELETE;
