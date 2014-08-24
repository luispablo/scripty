package com.duam.scripty;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.duam.scripty.activities.CommandActivity;
import com.duam.scripty.activities.MainActivity;
import com.duam.scripty.activities.ServerActivity;
import com.duam.scripty.db.Command;
import com.duam.scripty.db.ScriptyHelper;

import roboguice.util.Ln;

import static com.duam.scripty.db.ScriptyHelper.COMMAND;
import static com.duam.scripty.db.ScriptyHelper.COMMAND_ID;
import static com.duam.scripty.db.ScriptyHelper.COMMANDS_TABLE_NAME;
import static com.duam.scripty.db.ScriptyHelper.DESCRIPTION;
import static com.duam.scripty.db.ScriptyHelper.ID;
import static com.duam.scripty.db.ScriptyHelper.SERVER_ID;

import static com.duam.scripty.activities.CommandActivity.COMMAND_EDITED_RESULT;
import static com.duam.scripty.activities.CommandActivity.COMMAND_DELETED_RESULT;
import static com.duam.scripty.activities.ServerActivity.EDIT_SERVER_CODE;
import static com.duam.scripty.activities.ServerActivity.SERVER_SAVED;

/**
 * A fragment representing a list of Items.
 * <p />
 * <p />
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

        ScriptyHelper helper = ScriptyHelper.getInstance(getActivity());
        Cursor cursor = helper.getReadableDatabase().query(COMMANDS_TABLE_NAME, new String[]{ID, DESCRIPTION, COMMAND}, SERVER_ID+" = ?", new String[]{String.valueOf(serverId)}, null, null, null);
        Ln.d("Found "+ cursor.getCount() +" commands");

        adapter = new SimpleCursorAdapter(getActivity(), R.layout.command_list_item, cursor, new String[]{DESCRIPTION, COMMAND}, new int[]{android.R.id.text1, android.R.id.text2}, 0) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.command_list_item, parent, false);
                }

                Cursor c = (Cursor) getItem(position);

                ((TextView) convertView.findViewById(R.id.txtDescription)).setText(c.getString(c.getColumnIndexOrThrow(DESCRIPTION)));
                ((TextView) convertView.findViewById(R.id.txtCommand)).setText(c.getString(c.getColumnIndexOrThrow(COMMAND)));

                return convertView;
            }
        };
        setListAdapter(adapter);

        setHasOptionsMenu(true);
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

        if (requestCode == EDIT_SERVER_CODE) {
            switch (resultCode) {
                case SERVER_SAVED:
                    try {
                        ((MainActivity) getActivity().getParent()).loadServers();
                    } catch (IllegalAccessException | java.lang.InstantiationException e) {
                        Ln.e(e);
                    }
                    break;
            }
        } else {
            switch (resultCode) {
                case COMMAND_EDITED_RESULT:
                    refresh();
                    break;
                case COMMAND_DELETED_RESULT:
                    refresh();
                    break;
            }
        }
    }

    private void refresh() {
        ScriptyHelper helper = ScriptyHelper.getInstance(getActivity());
        Cursor cursor = helper.getReadableDatabase().query(COMMANDS_TABLE_NAME, new String[]{ID, DESCRIPTION, COMMAND}, SERVER_ID+" = ?", new String[]{String.valueOf(serverId)}, null, null, null);
        adapter.swapCursor(cursor);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Intent intent = new Intent(getActivity(), CommandActivity.class);
        intent.putExtra(COMMAND_ID, id);
        startActivityForResult(intent, COMMAND_ACTIONS_CODE);

        if (null != mListener) {
            // Notify the active callbacks interface (the activity, if the
            // fragment is attached to one) that an item has been selected.
//            mListener.onFragmentInteraction(DummyContent.ITEMS.get(position).id);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = false;

        switch (item.getItemId()) {
            case R.id.action_edit_server:
                editServer();
                return true;
            case R.id.action_delete_server:
                deleteServer();
                try {
                    ((MainActivity) getActivity().getParent()).loadServers();
                } catch (IllegalAccessException | java.lang.InstantiationException e) {
                    Ln.e(e);
                }
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void deleteServer() {
        ScriptyHelper helper = ScriptyHelper.getInstance(getActivity());

        for (Command cmd : helper.retrieveServerCommands(serverId)) {
            helper.deleteCommand(cmd.get_id());
        }

        helper.deleteServer(serverId);
    }

    private void editServer() {
        Intent intent = new Intent(getActivity(), ServerActivity.class);
        intent.putExtra(SERVER_ID, serverId);
        startActivityForResult(intent, EDIT_SERVER_CODE);
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
