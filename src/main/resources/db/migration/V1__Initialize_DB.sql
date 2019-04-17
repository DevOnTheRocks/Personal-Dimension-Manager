CREATE TABLE Dimensions
(
    ID      UUID     NOT NULL,
    Owner   UUID     NOT NULL,
    Name    VARCHAR(64),
    Created DATETIME NOT NULL,
    PRIMARY KEY (ID)
);

CREATE TABLE Members
(
    Dimension UUID NOT NULL,
    Player    UUID NOT NULL,
    PRIMARY KEY (Dimension, Player),
    FOREIGN KEY (Dimension) REFERENCES Dimensions (ID)
);

CREATE TABLE Locations
(
    Dimension UUID NOT NULL,
    Player    UUID NOT NULL,
    X         INT  NOT NULL,
    Y         INT  NOT NULL,
    Z         INT  NOT NULL,
    PRIMARY KEY (Dimension, Player),
    FOREIGN KEY (Dimension) REFERENCES Dimensions (ID)
);

CREATE TABLE Invites
(
    Dimension UUID NOT NULL,
    Sender    UUID NOT NULL,
    Receiver  UUID NOT NULL,
    PRIMARY KEY (Dimension, Sender, Receiver),
    FOREIGN KEY (Dimension) REFERENCES Dimensions (ID)
);
