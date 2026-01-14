port module Ports exposing (storeSession, loadSession)

import Platform.Cmd exposing (Cmd)


port storeSession : String -> Cmd msg


port loadSession : (String -> msg) -> Sub msg
