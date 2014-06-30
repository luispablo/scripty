package com.duam.scripty;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import roboguice.util.Ln;

import static com.duam.scripty.ScriptyHelper.COMMAND;
import static com.duam.scripty.ScriptyHelper.COMMAND_ID;
import static com.duam.scripty.ScriptyHelper.COMMANDS_TABLE_NAME;
import static com.duam.scripty.ScriptyHelper.DESCRIPTION;
import static com.duam.scripty.ScriptyHelper.ID;
import static com.duam.scripty.ScriptyHelper.SERVER_ID;

import static com.duam.scripty.CommandActionsActivity.COMMAND_EDITED_RESULT;


/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class CommandFragment extends ListFragment {

    private static final int COMMAND_ACTIONS_CODE = 10;

    private OnFragmentInteractionListener mListener;
    private SimpleCursorAdapter adapter;
    private long serverId;

    public static CommandFragment newInstance(Long serverId) {
        CommandFragment fragment = new CommandFragment();
        fragment.setServerId(serverId);
        Bundle args = new Bundle();
        args.putLong(SERVER_ID, serverId);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public CommandFragment() {
    }

    public void setServerId(long serverId) {
        this.serverId = serverId;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long serverId = getArguments().getLong(SERVER_ID);
        Ln.d("Loading commands from server " + serverId);

        ScriptyHelper helper = new ScriptyHelper(getActivity());
        Cursor cursor = helper.getReadableDatabase().query(COMMANDS_TABLE_NAME, new String[]{ID, DESCRIPTION, COMMAND}, SERVER_ID+" = ?", new String[]{String.valueOf(serverId)}, null, null, null);
        Ln.d("Found "+ cursor.getCount() +" commands");

        adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.two_line_list_item, cursor, new String[]{DESCRIPTION, COMMAND}, new int[]{android.R.id.text1, android.R.id.text2}, 0);
        setListAdapter(adapter);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (resultCode) {
            case COMMAND_EDITED_RESULT:
                ScriptyHelper helper = new ScriptyHelper(getActivity());
                Cursor cursor = helper.getReadableDatabase().query(COMMANDS_TABLE_NAME, new String[]{ID, DESCRIPTION, COMMAND}, SERVER_ID+" = ?", new String[]{String.valueOf(serverId)}, null, null, null);
                adapter.swapCursor(cursor);
                adapter.notifyDataSetChanged();
                break;
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent = new Intent(getActivity(), CommandActionsActivity.class);
        intent.putExtra(COMMAND_ID, id);
        startActivityForResult(intent, COMMAND_ACTIONS_CODE);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    /**
    * This interface must be implemented by activities that contain this
    * fragment to allow an interaction in this fragment to be communicated
    * to the activity and potentially other fragments contained in that
    * activity.
    * <p>
    * See the Android Training lesson <a href=
    * "http://developer.android.com/training/basics/fragments/communicating.html"
    * >Communicating with Other Fragments</a> for more information.
    */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(String id);
    }

}
