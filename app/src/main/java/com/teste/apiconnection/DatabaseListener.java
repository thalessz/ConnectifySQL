package com.teste.apiconnection;

import com.google.gson.JsonObject;

import java.util.List;

public interface DatabaseListener {
    void onQueryResult(List<JsonObject> result);
    void onInsertResult(boolean success);
}
