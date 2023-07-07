<pre>
-------------- INIT --------------
C: PacketHello
S: PacketWelcome
S: PacketLevelChecksum
if (C.unknownLevel) {
    C: PacketRequestLevel
    S: PacketLevel
}
C: PacketReadyToPlay

-------------- LOBBY --------------

C: PacketNewPlayer: {
    S: PacketPlayerCreated
}
C: PacketDeletePlayer: {
    S: PacketPlayerDeleted
}

C: PacketStart: {
    if(S.canStart) {
        S: PacketIngame
    }
}

-------------- INGAME --------------

C: PacketPress

S: PacketEntityData
S: PacketBallSpawn
S: PacketBallTrail







</pre>
